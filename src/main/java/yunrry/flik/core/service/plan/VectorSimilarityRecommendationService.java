package yunrry.flik.core.service.plan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.embedding.SpotSimilarity;
import yunrry.flik.core.service.user.UserCategoryVectorService;
import yunrry.flik.core.service.embedding.SpotEmbeddingService;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorSimilarityRecommendationService {

    private final UserCategoryVectorService userCategoryVectorService;
    private final SpotEmbeddingService spotEmbeddingService;

    public List<SpotSimilarity> findRecommendedSpotsByVectorSimilarity(Long userId, List<Long> candidateSpotIds, MainCategory category, int limit) {
        if (candidateSpotIds.isEmpty()) {
            return List.of();
        }

        try {
            userCategoryVectorService.recalculateCategoryVector(userId, category, candidateSpotIds);
            userCategoryVectorService.recalculateAllUserVectors(userId);
        }catch (Exception e){
            log.error("-Service-Failed to recalculate user category vector for userId: {}, category: {}", userId, category);
        }

        return spotEmbeddingService.findSimilarSpotsByUserPreference(userId, category.getCode(), candidateSpotIds, limit);

    }

}