package yunrry.flik.core.service.plan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.core.domain.model.plan.CourseSlot;
import yunrry.flik.core.domain.model.plan.TravelCourse;
import yunrry.flik.ports.in.query.CourseQuery;
import yunrry.flik.ports.out.repository.SpotRepository;
import yunrry.flik.ports.out.repository.TravelCourseRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateTravelCourseService {

    private final TravelCourseRecommendationService travelCourseRecommendationService;
    private final SpotRepository spotRepository;
    private final TravelCourseRepository travelCourseRepository;

    @Transactional
    public TravelCourse create(CourseQuery query) {
        try {
            // 1. 추천 코스 생성
            TravelCourse course = travelCourseRecommendationService
                    .generatePersonalizedTravelCourse(query);

            // 2. 모든 장소 ID 수집
            Set<Long> allSpotIds = collectAllSpotIds(course);

            if (allSpotIds.isEmpty()) {
                log.warn("No spots to process for course");
                course.updateTotalDistance(0.0); // 추가
                return travelCourseRepository.save(course);
            }

            // 3. 일괄 조회 및 캐싱
            Map<Long, Spot> spotCache = loadSpots(allSpotIds);

            // 4. 장소 선택
            List<List<Long>> allDaysSelectedSpots = processAllDays(course, spotCache);

            // 5. 총 이동거리 계산
            double totalDistance = calculateTotalDistance(allDaysSelectedSpots, spotCache);
            course.updateTotalDistance(totalDistance);

            // 6. 저장
            return travelCourseRepository.save(course);

        } catch (Exception e) {
            log.error("Failed to create travel course", e);
            throw new RuntimeException("여행 코스 생성에 실패했습니다.", e);
        }
    }


    private Set<Long> collectAllSpotIds(TravelCourse course) {
        Set<Long> allSpotIds = new HashSet<>();

        for (int day = 0; day < course.getDays(); day++) {
            int slots = (day == course.getDays() - 1 && course.getDays() > 1) ? 5 : 6;

            for (int slot = 0; slot < slots; slot++) {
                CourseSlot courseSlot = course.getSlot(day, slot);
                if (courseSlot != null && courseSlot.hasRecommendations()) {
                    allSpotIds.addAll(courseSlot.getRecommendedSpotIds());
                }
            }
        }

        log.debug("Collected {} unique spot IDs", allSpotIds.size());
        return allSpotIds;
    }

    private Map<Long, Spot> loadSpots(Set<Long> spotIds) {
        List<Spot> spots = spotRepository.findAllByIds(new ArrayList<>(spotIds));
        Map<Long, Spot> spotCache = spots.stream()
                .collect(Collectors.toMap(Spot::getId, spot -> spot));

        log.debug("Loaded {} spots into cache", spotCache.size());
        return spotCache;
    }

    private List<List<Long>> processAllDays(TravelCourse course, Map<Long, Spot> spotCache) {
        List<List<Long>> allDaysSelectedSpots = new ArrayList<>();
        Set<Long> globalSelectedSpotIds = new HashSet<>();

        for (int day = 0; day < course.getDays(); day++) {
            int slotsInDay = (day == course.getDays() - 1 && course.getDays() > 1) ? 5 : 6;
            List<Long> daySelectedSpots = processDay(
                    course, day, slotsInDay, globalSelectedSpotIds, spotCache);
            allDaysSelectedSpots.add(daySelectedSpots);
        }

        return allDaysSelectedSpots;
    }

    private List<Long> processDay(TravelCourse course, int day, int slotsInDay,
                                  Set<Long> globalSelectedSpotIds, Map<Long, Spot> spotCache) {
        List<Long> selectedSpotIds = new ArrayList<>();

        CourseSlot firstSlot = course.getSlot(day, 0);
        CourseSlot secondSlot = course.getSlot(day, 1);

        if (!firstSlot.hasRecommendations() || !secondSlot.hasRecommendations()) {
            log.warn("Day {} - Slot 0 or 1 has no recommendations", day + 1);
            return selectedSpotIds;
        }

        // 첫 두 슬롯 최근접 쌍 선택
        Long[] closestPair = findClosestPairByLocationWithExclusion(
                firstSlot.getRecommendedSpotIds(),
                secondSlot.getRecommendedSpotIds(),
                globalSelectedSpotIds,
                spotCache
        );

        if (closestPair[0] == null || closestPair[1] == null) {
            log.warn("Day {} - Could not find valid pair", day + 1);
            return selectedSpotIds;
        }

        firstSlot.selectSpot(closestPair[0]);
        secondSlot.selectSpot(closestPair[1]);
        globalSelectedSpotIds.add(closestPair[0]);
        globalSelectedSpotIds.add(closestPair[1]);
        selectedSpotIds.add(closestPair[0]);
        selectedSpotIds.add(closestPair[1]);

        Long previousSpotId = closestPair[1];
        log.debug("Day {} Slot 0={}, Slot 1={}", day + 1, closestPair[0], closestPair[1]);

        // 나머지 슬롯 순차 선택
        for (int i = 2; i < slotsInDay; i++) {
            CourseSlot currentSlot = course.getSlot(day, i);
            if (!currentSlot.hasRecommendations()) continue;

            List<Long> availableSpotIds = currentSlot.getRecommendedSpotIds().stream()
                    .filter(spotId -> !globalSelectedSpotIds.contains(spotId))
                    .collect(Collectors.toList());

            if (availableSpotIds.isEmpty()) {
                log.warn("Day {} Slot {} - No available spots", day + 1, i);
                continue;
            }

            Long closestSpotId = findClosestSpotByLocation(
                    previousSpotId, availableSpotIds, spotCache);

            if (closestSpotId != null) {
                currentSlot.selectSpot(closestSpotId);
                globalSelectedSpotIds.add(closestSpotId);
                previousSpotId = closestSpotId;
                selectedSpotIds.add(closestSpotId);
                log.debug("Day {} Slot {}={}", day + 1, i, closestSpotId);
            }
        }

        return selectedSpotIds;
    }

    private Long[] findClosestPairByLocationWithExclusion(
            List<Long> firstSpotIds,
            List<Long> secondSpotIds,
            Set<Long> excludedSpotIds,
            Map<Long, Spot> spotCache) {

        List<Long> filteredFirst = firstSpotIds.stream()
                .filter(id -> !excludedSpotIds.contains(id))
                .collect(Collectors.toList());

        List<Long> filteredSecond = secondSpotIds.stream()
                .filter(id -> !excludedSpotIds.contains(id))
                .collect(Collectors.toList());

        if (filteredFirst.isEmpty() || filteredSecond.isEmpty()) {
            return new Long[]{null, null};
        }

        return findClosestPairByLocation(filteredFirst, filteredSecond, spotCache);
    }

    private Long[] findClosestPairByLocation(
            List<Long> firstSpotIds,
            List<Long> secondSpotIds,
            Map<Long, Spot> spotCache) {

        Set<Long> firstSet = new HashSet<>(firstSpotIds);
        List<Long> filteredSecond = secondSpotIds.stream()
                .filter(id -> !firstSet.contains(id))
                .collect(Collectors.toList());

        if (filteredSecond.isEmpty()) {
            filteredSecond = secondSpotIds;
        }

        double minDistance = Double.MAX_VALUE;
        Long[] closestPair = new Long[2];

        for (Long spot1 : firstSpotIds) {
            Spot firstSpot = spotCache.get(spot1);
            if (firstSpot == null) continue;

            for (Long spot2 : filteredSecond) {
                if (spot1.equals(spot2)) continue;

                Spot secondSpot = spotCache.get(spot2);
                if (secondSpot == null) continue;

                double distance = calculateDistance(
                        firstSpot.getLatitude(), firstSpot.getLongitude(),
                        secondSpot.getLatitude(), secondSpot.getLongitude()
                );

                if (distance < minDistance) {
                    minDistance = distance;
                    closestPair[0] = spot1;
                    closestPair[1] = spot2;
                }
            }
        }

        return closestPair;
    }

    private Long findClosestSpotByLocation(
            Long baseSpotId,
            List<Long> candidateSpotIds,
            Map<Long, Spot> spotCache) {

        Spot baseSpot = spotCache.get(baseSpotId);
        if (baseSpot == null) {
            log.warn("Base spot not found in cache: {}", baseSpotId);
            return candidateSpotIds.isEmpty() ? null : candidateSpotIds.get(0);
        }

        List<Long> filteredCandidates = candidateSpotIds.stream()
                .filter(id -> !id.equals(baseSpotId))
                .collect(Collectors.toList());

        if (filteredCandidates.isEmpty()) {
            return candidateSpotIds.isEmpty() ? null : candidateSpotIds.get(0);
        }

        double minDistance = Double.MAX_VALUE;
        Long closestSpotId = null;

        for (Long candidateId : filteredCandidates) {
            Spot candidate = spotCache.get(candidateId);
            if (candidate == null) continue;

            double distance = calculateDistance(
                    baseSpot.getLatitude(), baseSpot.getLongitude(),
                    candidate.getLatitude(), candidate.getLongitude()
            );

            if (distance < minDistance) {
                minDistance = distance;
                closestSpotId = candidateId;
            }
        }

        return closestSpotId;
    }

    private double calculateTotalDistance(
            List<List<Long>> allDaysSelectedSpots,
            Map<Long, Spot> spotCache) {

        double totalDistance = 0.0;

        for (int day = 0; day < allDaysSelectedSpots.size(); day++) {
            List<Long> daySpots = allDaysSelectedSpots.get(day);
            if (daySpots.isEmpty()) continue;

            // 하루 내 이동거리
            for (int i = 0; i < daySpots.size() - 1; i++) {
                Spot from = spotCache.get(daySpots.get(i));
                Spot to = spotCache.get(daySpots.get(i + 1));

                if (from != null && to != null) {
                    totalDistance += calculateDistance(
                            from.getLatitude(), from.getLongitude(),
                            to.getLatitude(), to.getLongitude()
                    );
                }
            }

            // 다음날로 넘어가는 거리
            if (day < allDaysSelectedSpots.size() - 1) {
                List<Long> nextDaySpots = allDaysSelectedSpots.get(day + 1);
                if (!nextDaySpots.isEmpty()) {
                    Spot lastToday = spotCache.get(daySpots.get(daySpots.size() - 1));
                    Spot firstTomorrow = spotCache.get(nextDaySpots.get(0));

                    if (lastToday != null && firstTomorrow != null) {
                        totalDistance += calculateDistance(
                                lastToday.getLatitude(), lastToday.getLongitude(),
                                firstTomorrow.getLatitude(), firstTomorrow.getLongitude()
                        );
                    }
                }
            }
        }

        return totalDistance;
    }

    private double calculateDistance(
            BigDecimal lat1, BigDecimal lon1,
            BigDecimal lat2, BigDecimal lon2) {

        final int R = 6371; // km

        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lon1Rad = Math.toRadians(lon1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double lon2Rad = Math.toRadians(lon2.doubleValue());

        double latDiff = lat2Rad - lat1Rad;
        double lonDiff = lon2Rad - lon1Rad;

        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}