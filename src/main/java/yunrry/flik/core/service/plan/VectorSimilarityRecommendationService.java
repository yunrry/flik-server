package yunrry.flik.core.service.plan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import yunrry.flik.core.domain.exception.RecommendationException;
import yunrry.flik.core.domain.exception.VectorCalculationException;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.embedding.SpotSimilarity;
import yunrry.flik.core.service.embedding.SpotEmbeddingService;
import yunrry.flik.core.service.user.UserCategoryVectorService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorSimilarityRecommendationService {

    private final UserCategoryVectorService userCategoryVectorService;
    private final SpotEmbeddingService spotEmbeddingService;

    /**
     * 벡터 유사도 기반으로 장소 추천 (SpotSimilarity 반환)
     *
     * @param userId 사용자 ID
     * @param candidateSpotIds 후보 장소 ID 목록
     * @param category 카테고리
     * @param limit 추천 개수 제한
     * @return 유사도와 함께 추천된 장소 목록
     */
    @Transactional
    public List<SpotSimilarity> findRecommendedSpotsByVectorSimilarity(
            Long userId,
            List<Long> candidateSpotIds,
            MainCategory category,
            int limit) {

        if (candidateSpotIds == null || candidateSpotIds.isEmpty()) {
            log.warn("No candidate spots provided for userId: {}, category: {}", userId, category);
            return List.of();
        }

        if (limit <= 0) {
            log.warn("Invalid limit: {} for userId: {}, category: {}", limit, userId, category);
            return List.of();
        }

        log.debug("Finding recommended spots for userId: {}, category: {}, candidates: {}, limit: {}",
                userId, category.getCode(), candidateSpotIds.size(), limit);

        try {
            // 1. 사용자 카테고리 벡터 재계산
            recalculateUserVectors(userId, category, candidateSpotIds);

            // 2. 유사도 기반 장소 추천
            List<SpotSimilarity> recommendations = spotEmbeddingService
                    .findSimilarSpotsByUserPreference(
                            userId,
                            category.getCode(),
                            candidateSpotIds,
                            limit
                    );

            log.info("Found {} recommended spots for userId: {}, category: {}",
                    recommendations.size(), userId, category.getCode());

            return recommendations;

        } catch (Exception e) {
            log.error("Failed to find recommended spots for userId: {}, category: {}",
                    userId, category, e);
            throw new RecommendationException(
                    String.format("장소 추천에 실패했습니다. (userId: %d, category: %s)", userId, category),
                    e
            );
        }
    }

    /**
     * 벡터 유사도 기반으로 장소 추천 (ID만 반환)
     *
     * @param userId 사용자 ID
     * @param candidateSpotIds 후보 장소 ID 목록
     * @param category 카테고리
     * @param limit 추천 개수 제한
     * @return 추천된 장소 ID 목록
     */
    @Transactional
    public List<Long> findRecommendedSpotIdsByVectorSimilarity(
            Long userId,
            List<Long> candidateSpotIds,
            MainCategory category,
            int limit) {

        return findRecommendedSpotsByVectorSimilarity(userId, candidateSpotIds, category, limit)
                .stream()
                .map(SpotSimilarity::spotId)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 벡터 재계산
     *
     * @param userId 사용자 ID
     * @param category 카테고리
     * @param candidateSpotIds 후보 장소 ID 목록
     */
    private void recalculateUserVectors(Long userId, MainCategory category, List<Long> candidateSpotIds) {
        try {
            log.debug("Recalculating vectors for userId: {}, category: {}", userId, category);

            // 카테고리별 벡터 재계산
            userCategoryVectorService.recalculateCategoryVector(userId, category, candidateSpotIds);

            // 전체 사용자 벡터 재계산
            userCategoryVectorService.recalculateAllUserVectors(userId);

            log.debug("Successfully recalculated vectors for userId: {}, category: {}", userId, category);

        } catch (Exception e) {
            log.error("Failed to recalculate user vectors for userId: {}, category: {}",
                    userId, category, e);
            throw new VectorCalculationException(
                    String.format("사용자 벡터 계산에 실패했습니다. (userId: %d, category: %s)", userId, category),
                    e
            );
        }
    }
}