package yunrry.flik.core.service.plan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.core.domain.model.plan.CourseSlot;
import yunrry.flik.core.domain.model.plan.TravelCourse;
import yunrry.flik.ports.in.query.CourseQuery;
import yunrry.flik.ports.out.repository.SpotRepository;
import yunrry.flik.ports.out.repository.TravelCourseRepository;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateTravelCourseService {

    private final TravelCourseRecommendationService travelCourseRecommendationService;
    private final SpotRepository spotRepository;
    private final TravelCourseRepository travelCourseRepository;

    /**
     * 여행 코스 생성
     */
    public Mono<TravelCourse> create(CourseQuery query) {
        return new Mono<TravelCourse>() {
            @Override
            public void subscribe(CoreSubscriber<? super TravelCourse> coreSubscriber) {

            }
        };
//        return travelCourseRecommendationService.generatePersonalizedTravelCourse(query)
//                .flatMap(travelCourse ->
//                        Mono.fromCallable(() -> {
//                                    int days = travelCourse.getDays();
//                                    Set<Long> globalSelectedSpotIds = new HashSet<>();
//
//                                    // 일자별 최종 선택된 SpotId를 담을 리스트
//                                    List<List<Long>> allDaysSelectedSpotIds = new java.util.ArrayList<>();
//
//                                    for (int day = 0; day < days; day++) {
//                                        int slotsInDay = (day == days - 1 && days > 1) ? 5 : 6; // 마지막 날은 5칸
//                                        List<Long> daySelectedSpotIds = processDay(travelCourse, day, slotsInDay, globalSelectedSpotIds);
//
//                                        allDaysSelectedSpotIds.add(daySelectedSpotIds);
//                                    }
//
//                                    // 1. 전체 여행의 총 이동 거리 계산
//                                    double totalDistance = calculateTotalDistance(allDaysSelectedSpotIds);
//                                    travelCourse.updateTotalDistance(totalDistance);
//
//                                    return travelCourse;
//                                })
//                                // JPA 호출이 있으므로 별도의 스레드 풀에서 실행
//                                .subscribeOn(Schedulers.boundedElastic())
//                )
//                .flatMap(completedTravelCourse ->
//                        // 2. DB에 저장
//                        Mono.fromCallable(() -> travelCourseRepository.save(completedTravelCourse))
//                                .subscribeOn(Schedulers.boundedElastic())
//                )
//                .doOnSuccess(tc -> log.info("Generated full travel course with id={} totalDistance={} km",
//                        tc.getId(), tc.getTotalDistance()))
//                .doOnError(err -> log.error("Failed to create travel course", err));
    }

    /**
     * 하루 코스를 순회하며 슬롯별 추천 스팟 결정
     */
    private List<Long> processDay(TravelCourse travelCourse, int day, int slotsInDay, Set<Long> globalSelectedSpotIds) {
        List<Long> selectedSpotIds = new java.util.ArrayList<>();

        CourseSlot firstSlot = travelCourse.getSlot(day, 0);
        CourseSlot secondSlot = travelCourse.getSlot(day, 1);
        Long previousSpotId;

        if (!firstSlot.hasRecommendations() || !secondSlot.hasRecommendations()) {
            log.warn("Day {} - Slot 0 or Slot 1 has no recommendations", day + 1);
            return selectedSpotIds;
        }

        // 1. 첫 슬롯과 두 번째 슬롯의 가장 가까운 조합 찾기 (이전에 선택된 스팟 제외)
        Long[] closestPair = findClosestPairByLocationWithExclusion(
                firstSlot.getRecommendedSpotIds(),
                secondSlot.getRecommendedSpotIds(),
                globalSelectedSpotIds
        );

        if (closestPair[0] == null || closestPair[1] == null) {
            log.warn("Day {} - Could not find valid pair for first two slots", day + 1);
            return selectedSpotIds;
        }

        firstSlot.selectSpot(closestPair[0]);
        secondSlot.selectSpot(closestPair[1]);

        globalSelectedSpotIds.add(closestPair[0]);
        globalSelectedSpotIds.add(closestPair[1]);
        previousSpotId = closestPair[1];

        // 하루 순서 리스트에 추가
        selectedSpotIds.add(closestPair[0]);
        selectedSpotIds.add(closestPair[1]);

        log.info("Day {} Slot 0 -> {}, Slot 1 -> {}", day + 1, closestPair[0], closestPair[1]);

        // 2. 세 번째 슬롯부터 순차적으로 결정
        for (int i = 2; i < slotsInDay; i++) {
            CourseSlot currentSlot = travelCourse.getSlot(day, i);
            if (!currentSlot.hasRecommendations()) continue;

            List<Long> availableSpotIds = currentSlot.getRecommendedSpotIds().stream()
                    .filter(spotId -> !globalSelectedSpotIds.contains(spotId))
                    .collect(Collectors.toList());

            if (availableSpotIds.isEmpty()) {
                log.warn("Day {} Slot {} - No available spots after filtering globally selected ones", day + 1, i);
                continue;
            }

            Long closestSpotId = findClosestSpotByLocation(previousSpotId, availableSpotIds);
            currentSlot.selectSpot(closestSpotId);
            globalSelectedSpotIds.add(closestSpotId);
            previousSpotId = closestSpotId;

            selectedSpotIds.add(closestSpotId);

            log.info("Day {} Slot {} -> {}", day + 1, i, closestSpotId);
        }

        return selectedSpotIds;
    }

    /**
     * Object를 안전하게 Long으로 변환
     */
    private Long extractSpotId(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("SpotId value is null");
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot parse String to Long: " + value);
            }
        }
        throw new IllegalArgumentException("Expected Number or String but got: " + value.getClass() + " with value: " + value);
    }


    /**
     * 두 지점 간의 거리 계산 (단위: km)
     */
    private double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        final int R = 6371; // 지구 반지름 (km)

        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lon1Rad = Math.toRadians(lon1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double lon2Rad = Math.toRadians(lon2.doubleValue());

        double latDistance = lat2Rad - lat1Rad;
        double lonDistance = lon2Rad - lon1Rad;

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * 두 슬롯의 추천 스팟들 중 가장 가까운 조합 찾기 (이미 선택된 스팟 제외)
     */
    private Long[] findClosestPairByLocationWithExclusion(List<Long> firstSpotIds, List<Long> secondSpotIds, Set<Long> excludedSpotIds) {
        // Filter out already selected spots from both lists
        List<Long> filteredFirstSpots = firstSpotIds.stream()
                .filter(spotId -> !excludedSpotIds.contains(spotId))
                .collect(Collectors.toList());

        List<Long> filteredSecondSpots = secondSpotIds.stream()
                .filter(spotId -> !excludedSpotIds.contains(spotId))
                .collect(Collectors.toList());

        if (filteredFirstSpots.isEmpty() || filteredSecondSpots.isEmpty()) {
            return new Long[]{null, null};
        }

        return findClosestPairByLocation(filteredFirstSpots, filteredSecondSpots);
    }

    /**
     * 두 그룹에서 가장 가까운 스팟 쌍 찾기
     */
    private Long[] findClosestPairByLocation(List<Long> firstSpotIds, List<Long> secondSpotIds) {
        // Remove duplicates from the second group to avoid selecting the same spot twice
        List<Long> filteredSecondSpotIds = secondSpotIds.stream()
                .filter(spotId -> !firstSpotIds.contains(spotId))
                .collect(Collectors.toList());

        // If no unique spots remain in second group, use original list
        if (filteredSecondSpotIds.isEmpty()) {
            log.warn("No unique spots found between groups, using original second group");
            filteredSecondSpotIds = secondSpotIds;
        }

        double minDistance = Double.MAX_VALUE;
        Long[] closestPair = new Long[2];

        for (Long spot1 : firstSpotIds) {
            Spot firstSpot = spotRepository.findById(spot1);

            for (Long spot2 : filteredSecondSpotIds) {
                // Skip if same spot (additional safety check)
                if (spot1.equals(spot2)) continue;

                Spot secondSpot = spotRepository.findById(spot2);

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

    /**
     * 기준 스팟에서 가장 가까운 스팟 찾기
     */
    private Long findClosestSpotByLocation(Long baseSpotId, List<Long> candidateSpotIds) {
        Spot baseSpot = spotRepository.findById(baseSpotId);
        double minDistance = Double.MAX_VALUE;
        Long closestSpotId = null;

        // Filter out the baseSpotId from candidates to avoid selecting the same spot
        List<Long> filteredCandidateSpotIds = candidateSpotIds.stream()
                .filter(spotId -> !spotId.equals(baseSpotId))  // Fixed: compare with baseSpotId, not candidateSpotIds
                .collect(Collectors.toList());

        // If no candidates remain after filtering, handle gracefully
        if (filteredCandidateSpotIds.isEmpty()) {
            log.warn("No valid candidates found after filtering baseSpotId: {}", baseSpotId);
            return candidateSpotIds.isEmpty() ? null : candidateSpotIds.get(0);
        }

        // Iterate over filtered candidates, not the original list
        for (Long candidateId : filteredCandidateSpotIds) {
            Spot candidate = spotRepository.findById(candidateId);

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

    private double calculateTotalDistance(List<List<Long>> allDaysSelectedSpotIds) {
        double totalDistance = 0.0;

        for (int day = 0; day < allDaysSelectedSpotIds.size(); day++) {
            List<Long> daySpots = allDaysSelectedSpotIds.get(day);

            // 하루 내부의 이동 거리 계산
            for (int i = 0; i < daySpots.size() - 1; i++) {
                Spot from = spotRepository.findById(daySpots.get(i));
                Spot to = spotRepository.findById(daySpots.get(i + 1));

                totalDistance += calculateDistance(from.getLatitude(), from.getLongitude(),
                        to.getLatitude(), to.getLongitude());
            }

            // 다음날로 넘어가는 거리 계산 (마지막 스팟 → 다음날 첫 스팟)
            if (day < allDaysSelectedSpotIds.size() - 1 && !daySpots.isEmpty() && !allDaysSelectedSpotIds.get(day + 1).isEmpty()) {
                Long lastSpotToday = daySpots.get(daySpots.size() - 1);
                Long firstSpotNextDay = allDaysSelectedSpotIds.get(day + 1).get(0);

                Spot lastSpot = spotRepository.findById(lastSpotToday);
                Spot firstSpot = spotRepository.findById(firstSpotNextDay);

                totalDistance += calculateDistance(lastSpot.getLatitude(), lastSpot.getLongitude(),
                        firstSpot.getLatitude(), firstSpot.getLongitude());
            }
        }

        return totalDistance;
    }
}