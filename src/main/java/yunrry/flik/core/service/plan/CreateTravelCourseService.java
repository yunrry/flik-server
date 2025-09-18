package yunrry.flik.core.service.plan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import yunrry.flik.adapters.out.persistence.mysql.repository.SpotJpaRepository;
import yunrry.flik.adapters.out.persistence.postgres.repository.SpotEmbeddingJpaRepository;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.core.domain.model.plan.CourseSlot;
import yunrry.flik.core.domain.model.plan.TravelCourse;
import yunrry.flik.ports.in.query.CourseQuery;
import yunrry.flik.ports.out.repository.SpotRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateTravelCourseService {

    private final TravelCourseRecommendationService travelCourseRecommendationService;
    private final SpotEmbeddingJpaRepository spotEmbeddingRepository;
    private final SpotRepository spotRepository;

    /**
     * 여행 코스 생성
     */
    public Mono<TravelCourse> create(CourseQuery query) {
        return travelCourseRecommendationService.generatePersonalizedTravelCourse(query)
                .flatMap(travelCourse ->
                        Mono.fromCallable(() -> {
                                    int days = travelCourse.getDays();

                                    for (int day = 0; day < days; day++) {
                                        int slotsInDay = (day == days - 1 && days > 1) ? 5 : 6; // 마지막 날은 5칸
                                        processDay(travelCourse, day, slotsInDay);
                                    }
                                    return travelCourse;
                                })
                                // JPA 호출이 있으므로 별도의 스레드 풀에서 실행
                                .subscribeOn(Schedulers.boundedElastic())
                )
                .doOnSuccess(tc -> log.info("Generated full travel course with {} days", tc.getDays()))
                .doOnError(err -> log.error("Failed to create travel course", err));
    }

    /**
     * 하루 코스를 순회하며 슬롯별 추천 스팟 결정
     */
    private void processDay(TravelCourse travelCourse, int day, int slotsInDay) {
        CourseSlot firstSlot = travelCourse.getSlot(day, 0);
        CourseSlot secondSlot = travelCourse.getSlot(day, 1);
        Long previousSpotId;

        if (!firstSlot.hasRecommendations() || !secondSlot.hasRecommendations()) {
            log.warn("Day {} - Slot 0 or Slot 1 has no recommendations", day + 1);
            return;
        }

        // 1. 첫 슬롯과 두 번째 슬롯의 가장 가까운 조합 찾기
        Long[] closestPair = findClosestPairByLocation(
                firstSlot.getRecommendedSpotIds(),
                secondSlot.getRecommendedSpotIds()
        );

        firstSlot.selectSpot(closestPair[0]);
        secondSlot.selectSpot(closestPair[1]);
        previousSpotId = closestPair[1];

        log.info("Day {} Slot 0 -> {}, Slot 1 -> {}", day + 1, closestPair[0], closestPair[1]);

        // 2. 세 번째 슬롯부터 순차적으로 결정
        for (int i = 2; i < slotsInDay; i++) {
            CourseSlot currentSlot = travelCourse.getSlot(day, i);
            if (!currentSlot.hasRecommendations()) continue;

            Long closestSpotId = findClosestSpotByLocation(previousSpotId, currentSlot.getRecommendedSpotIds());
            currentSlot.selectSpot(closestSpotId);
            previousSpotId = closestSpotId;

            log.info("Day {} Slot {} -> {}", day + 1, i, closestSpotId);
        }
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
     * 두 그룹에서 가장 가까운 스팟 쌍 찾기
     */
    private Long[] findClosestPairByLocation(List<Long> firstSpotIds, List<Long> secondSpotIds) {
        double minDistance = Double.MAX_VALUE;
        Long[] closestPair = new Long[2];

        for (Long spot1 : firstSpotIds) {
            Spot firstSpot = spotRepository.findById(spot1);

            for (Long spot2 : secondSpotIds) {
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

        for (Long candidateId : candidateSpotIds) {
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
}