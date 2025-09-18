//package yunrry.flik.core.service.plan;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import yunrry.flik.core.domain.model.MainCategory;
//import yunrry.flik.core.domain.model.card.Spot;
//import yunrry.flik.core.service.embedding.SpotEmbeddingService;
//import yunrry.flik.core.service.embedding.VectorProcessingService;
//import yunrry.flik.core.service.user.UserCategoryVectorService;
//import yunrry.flik.core.service.user.UserPreferenceService;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class VectorSimilarityRecommendationServiceBackUp {
//
//    private final UserCategoryVectorService userCategoryVectorService;
//    private final VectorProcessingService vectorProcessingService;
//    private final SpotEmbeddingService spotEmbeddingService;
//    private final UserPreferenceService userPreferenceService;
//
//    public List<Spot> findRecommendedSpotsByVectorSimilarity(Long userId,
//                                                             List<Spot> candidateSpots,
//                                                             MainCategory category,
//                                                             int limit) {
//        if (candidateSpots.isEmpty()) {
//            return List.of();
//        }
//
//        int categorySavedCount = userPreferenceService.getUserMainCategoryCount(userId, category.getDisplayName());
//
//        // 1. 사용자의 해당 카테고리 선호 벡터 조회
//        String userPreferenceVector = userCategoryVectorService.getUserCategoryPreferenceVector(userId, category, categorySavedCount);
//
//        if (userPreferenceVector == null || userPreferenceVector.isEmpty()) {
//            Collections.shuffle(candidateSpots);
//            return candidateSpots.stream().limit(limit).collect(Collectors.toList());
//        }
//
//        // 2. 각 후보 장소와 사용자 선호 벡터 간 유사도 계산
//        List<SpotSimilarity> similarities = candidateSpots.stream()
//                .map(spot -> calculateSpotSimilarity(spot, userPreferenceVector))
//                .filter(Objects::nonNull)
//                .sorted(Comparator.comparingDouble(SpotSimilarity::similarity).reversed())
//                .collect(Collectors.toList());
//
//        // 3. 상위 유사도 장소들 반환
//        return similarities.stream()
//                .limit(limit)
//                .map(SpotSimilarity::spot)
//                .collect(Collectors.toList());
//    }
//
//    private SpotSimilarity calculateSpotSimilarity(Spot spot, String userPreferenceVector) {
//        try {
//            // 장소의 태그 임베딩 벡터 생성 또는 조회
//            String spotTagVector = getSpotTagVector(spot);
//            if (spotTagVector == null || spotTagVector.isEmpty()) {
//                return null;
//            }
//
//            // 코사인 유사도 계산
//            double similarity = vectorProcessingService.calculateCosineSimilarity(
//                    userPreferenceVector, spotTagVector);
//
//            return new SpotSimilarity(spot, similarity);
//
//        } catch (Exception e) {
//            log.warn("Failed to calculate similarity for spot {}: {}", spot.getId(), e.getMessage());
//            return null;
//        }
//    }
//
//    private String getSpotTagVector(Spot spot) {
//        // 1. SpotEmbedding에서 기존 벡터 조회
//        Optional<String> existingVector = spotEmbeddingService.getTagEmbedding(spot.getId());
//
//        if (existingVector.isPresent()) {
//            return existingVector.get();
//        }else{
//            log.info("No existing tag embedding for spot {}. Generating new one.", spot.getId());
//            try {
//                Thread.sleep(1000); // 1초 대기
//
//                // 재조회 시도
//                Optional<String> existingVectorRe = spotEmbeddingService.getTagEmbedding(spot.getId());
//
//                if (existingVector.isPresent()) {
//                    return existingVectorRe.get();
//                }else{
//                    log.info("Still no tag embedding after wait for spot {}. Generating new one.", spot.getId());
//                }
//
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                log.error("Thread interrupted while waiting for embeddings: {}", e.getMessage());
//            }
//        }
//
//        // 2. 없으면 디폴트 벡터 리턴
//        return vectorProcessingService.generateDefaultTagVector();
//    }
//
//    private record SpotSimilarity(Spot spot, double similarity) {}
//}