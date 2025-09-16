//package yunrry.flik.core.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//import reactor.core.scheduler.Schedulers;
//
//import yunrry.flik.core.domain.model.TravelPlan;
//import yunrry.flik.core.domain.model.UserPreference;
//import yunrry.flik.ports.out.repository.UserSavedSpotRepository;
//import yunrry.flik.ports.out.repository.SpotEmbeddingRepository;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class TravelPlanRecommendationService {
//
//    private final UserSavedSpotRepository userSavedSpotRepository;
//    private final SpotEmbeddingRepository spotEmbeddingRepository;
//    private final VectorProcessingService vectorProcessingService;
//
//    /**
//     * 사용자 맞춤 여행 플랜 추천
//     */
//    public Mono<TravelPlan> recommendTravelPlan(Long userId,
//                                                double locationWeight,
//                                                double tagWeight,
//                                                int maxSpots) {
//        return getUserSavedSpotIds(userId)
//                .flatMap(savedSpotIds -> {
//                    if (savedSpotIds.isEmpty()) {
//                        log.info("No saved spots found for user: {}", userId);
//                        return Mono.just(TravelPlan.empty(userId));
//                    }
//
//                    log.info("Found {} saved spots for user: {}", savedSpotIds.size(), userId);
//
//                    // 사용자 선호도 벡터 계산
//                    return calculateUserPreferenceVectors(savedSpotIds)
//                            .flatMap(preferenceVectors ->
//                                    findSimilarSpots(
//                                            savedSpotIds,
//                                            preferenceVectors.locationVector(),
//                                            preferenceVectors.tagVector(),
//                                            locationWeight,
//                                            tagWeight,
//                                            maxSpots
//                                    )
//                            )
//                            .map(recommendedSpotIds ->
//                                    TravelPlan.of(userId, recommendedSpotIds, locationWeight, tagWeight)
//                            );
//                })
//                .doOnSuccess(travelPlan ->
//                        log.info("Generated travel plan with {} spots for user: {}",
//                                travelPlan.getSpotIds().size(), userId))
//                .doOnError(error ->
//                        log.error("Failed to generate travel plan for user: {} - Error: {}",
//                                userId, error.getMessage()));
//    }
//
//    /**
//     * 지역 기반 여행 플랜 추천
//     */
//    public Mono<TravelPlan> recommendTravelPlanByRegion(Long userId,
//                                                        double latitude,
//                                                        double longitude,
//                                                        double maxDistance,
//                                                        double locationWeight,
//                                                        double tagWeight,
//                                                        int maxSpots) {
//        return getUserSavedSpotIds(userId)
//                .flatMap(savedSpotIds -> {
//                    if (savedSpotIds.isEmpty()) {
//                        return Mono.just(TravelPlan.empty(userId));
//                    }
//
//                    // 지정된 위치 주변의 유사한 장소 찾기
//                    return vectorProcessingService.createLocationEmbedding(
//                                    java.math.BigDecimal.valueOf(latitude),
//                                    java.math.BigDecimal.valueOf(longitude))
//                            .flatMap(targetLocationVector ->
//                                    calculateUserTagPreference(savedSpotIds)
//                                            .flatMap(userTagVector ->
//                                                    findSimilarSpotsInRegion(
//                                                            targetLocationVector,
//                                                            userTagVector,
//                                                            maxDistance,
//                                                            locationWeight,
//                                                            tagWeight,
//                                                            maxSpots
//                                                    )
//                                            )
//                            )
//                            .map(recommendedSpotIds ->
//                                    TravelPlan.ofRegion(userId, recommendedSpotIds, latitude, longitude, maxDistance)
//                            );
//                });
//    }
//
//    private Mono<List<Long>> getUserSavedSpotIds(Long userId) {
//        return Mono.fromCallable(() -> userSavedSpotRepository.findSpotIdsByUserId(userId))
//                .subscribeOn(Schedulers.boundedElastic());
//    }
//
//    private Mono<PreferenceVectors> calculateUserPreferenceVectors(List<Long> spotIds) {
//        return Mono.fromCallable(() -> {
//                    List<SpotEmbedding> embeddings = spotEmbeddingRepository.findBySpotIds(spotIds);
//
//                    if (embeddings.isEmpty()) {
//                        return new PreferenceVectors("[0.0,0.0]", generateDefaultTagVector());
//                    }
//
//                    // 위치 벡터들의 평균 계산
//                    String avgLocationVector = calculateAverageLocationVector(embeddings);
//                    // 태그 벡터들의 평균 계산
//                    String avgTagVector = calculateAverageTagVector(embeddings);
//
//                    return new PreferenceVectors(avgLocationVector, avgTagVector);
//                })
//                .subscribeOn(Schedulers.boundedElastic());
//    }
//
//    private Mono<String> calculateUserTagPreference(List<Long> spotIds) {
//        return Mono.fromCallable(() -> {
//                    List<SpotEmbedding> embeddings = spotEmbeddingRepository.findBySpotIds(spotIds);
//                    return calculateAverageTagVector(embeddings);
//                })
//                .subscribeOn(Schedulers.boundedElastic());
//    }
//
//    private Mono<List<Long>> findSimilarSpots(List<Long> savedSpotIds,
//                                              String locationVector,
//                                              String tagVector,
//                                              double locationWeight,
//                                              double tagWeight,
//                                              int maxSpots) {
//        return Mono.fromCallable(() -> {
//                    // 사용자가 저장한 장소들을 제외하고 유사한 장소 검색
//                    return spotEmbeddingRepository.findSimilarSpotsExcluding(
//                            savedSpotIds, locationVector, tagVector, locationWeight, tagWeight, maxSpots);
//                })
//                .subscribeOn(Schedulers.boundedElastic());
//    }
//
//    private Mono<List<Long>> findSimilarSpotsInRegion(String locationVector,
//                                                      String tagVector,
//                                                      double maxDistance,
//                                                      double locationWeight,
//                                                      double tagWeight,
//                                                      int maxSpots) {
//        return Mono.fromCallable(() -> {
//                    return spotEmbeddingRepository.findSimilarSpotsInRegion(
//                            locationVector, tagVector, maxDistance, locationWeight, tagWeight, maxSpots);
//                })
//                .subscribeOn(Schedulers.boundedElastic());
//    }
//
//    private String calculateAverageLocationVector(List<SpotEmbedding> embeddings) {
//        if (embeddings.isEmpty()) {
//            return "[0.0,0.0]";
//        }
//
//        double sumLat = 0.0;
//        double sumLng = 0.0;
//        int count = 0;
//
//        for (SpotEmbedding embedding : embeddings) {
//            List<Double> locationVec = parseLocationVector(embedding.getLocationEmbedding());
//            if (locationVec.size() >= 2) {
//                sumLat += locationVec.get(0);
//                sumLng += locationVec.get(1);
//                count++;
//            }
//        }
//
//        if (count == 0) {
//            return "[0.0,0.0]";
//        }
//
//        double avgLat = sumLat / count;
//        double avgLng = sumLng / count;
//
//        return String.format("[%f,%f]", avgLat, avgLng);
//    }
//
//    private String calculateAverageTagVector(List<SpotEmbedding> embeddings) {
//        if (embeddings.isEmpty()) {
//            return generateDefaultTagVector();
//        }
//
//        List<List<Double>> tagVectors = embeddings.stream()
//                .map(embedding -> parseTagVector(embedding.getTagEmbedding()))
//                .filter(vec -> !vec.isEmpty())
//                .collect(java.util.stream.Collectors.toList());
//
//        if (tagVectors.isEmpty()) {
//            return generateDefaultTagVector();
//        }
//
//        List<Double> averageVector = vectorProcessingService.averageVectors(tagVectors);
//        return vectorProcessingService.formatVector(averageVector);
//    }
//
//    private List<Double> parseLocationVector(String vectorString) {
//        return vectorProcessingService.parseVector(vectorString);
//    }
//
//    private List<Double> parseTagVector(String vectorString) {
//        return vectorProcessingService.parseVector(vectorString);
//    }
//
//    private String generateDefaultTagVector() {
//        // 1536차원의 기본 벡터 (모든 값이 0.0)
//        List<Double> defaultVector = java.util.Collections.nCopies(1536, 0.0);
//        return vectorProcessingService.formatVector(defaultVector);
//    }
//
//    private record PreferenceVectors(String locationVector, String tagVector) {}
//}