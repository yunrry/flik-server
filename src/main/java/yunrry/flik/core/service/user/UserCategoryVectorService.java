package yunrry.flik.core.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import yunrry.flik.core.domain.mapper.CategoryMapper;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.core.domain.model.embedding.SpotEmbedding;
import yunrry.flik.core.service.embedding.SpotEmbeddingService;
import yunrry.flik.core.service.embedding.VectorProcessingService;
import yunrry.flik.ports.out.repository.SpotRepository;
import yunrry.flik.ports.out.repository.UserCategoryVectorRepository;
import yunrry.flik.ports.out.repository.UserSavedSpotRepository;


import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserCategoryVectorService {

    private final UserSavedSpotRepository userSavedSpotRepository;
    private final SpotRepository spotRepository;
    private final UserCategoryVectorRepository userCategoryVectorRepository;
    private final VectorProcessingService vectorProcessingService;
    private final CategoryMapper categoryMapper;
    private final SpotEmbeddingService spotEmbeddingService;

    @Cacheable(value = "userCategoryVectors", key = "#userId + '_' + #category.code + '_' + #userCategorySavedCount")
    public String getUserCategoryPreferenceVector(Long userId, MainCategory category, int userCategorySavedCount) {
        // savedCount가 변경되면 새로 계산하고 DB 업데이트
        String vector = calculateCategoryPreferenceVector(userId, category);

        // 비동기로 DB 업데이트
        updateSingleCategoryVector(userId, category, vector);

        return vector;
    }

    private void updateSingleCategoryVector(Long userId, MainCategory category, String vector) {
        Mono.fromRunnable(() -> {
                    Map<MainCategory, String> singleCategoryMap = Map.of(category, vector);
                    userCategoryVectorRepository.saveUserCategoryVectors(userId, singleCategoryMap);
                    log.debug("Updated vector for user: {}, category: {}", userId, category);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    private String calculateCategoryPreferenceVector(Long userId, MainCategory category) {
        // 1. 사용자 저장 장소 조회
        List<Long> savedSpotIds = userSavedSpotRepository.findSpotIdsByUserId(userId);
        if (savedSpotIds.isEmpty()) {
            return getDefaultCategoryVector(category);
        }

        List<String> subCategories = categoryMapper.getSubCategoryNames(category);

        // 2. 해당 카테고리의 장소들만 필터링
        List<Spot> categorySpots = spotRepository.findByIdsAndLabelDepth2In(savedSpotIds, subCategories);
        if (categorySpots.isEmpty()) {
            return getDefaultCategoryVector(category);
        }

        // 3. 저장된 장소 태그 임베딩들 조회
        List<Long> categorySpotIds = categorySpots.stream()
                .map(Spot::getId)
                .collect(Collectors.toList());

        List<SpotEmbedding> embeddings = spotEmbeddingService.getEmbeddingsBySpotIds(categorySpotIds);

        if(embeddings.size() < categorySpotIds.size()){
            log.warn("Some embeddings are missing for user. should wait or skip: {} in category: {}", userId, category);

            try {
                Thread.sleep(2000); // 2초 대기

                // 재조회 시도
                embeddings = spotEmbeddingService.getEmbeddingsBySpotIds(categorySpotIds);

                if(embeddings.size() < categorySpotIds.size()){
                    log.warn("Still missing embeddings after wait for user: {} in category: {}", userId, category);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Thread interrupted while waiting for embeddings: {}", e.getMessage());
            }
        }

        List<String> tagVectors = embeddings.stream()
                .filter(SpotEmbedding::hasTagEmbedding)
                .map(SpotEmbedding::getTagEmbeddingAsString)
                .collect(Collectors.toList());

        if (tagVectors.isEmpty()) {
            log.debug("No tag embeddings found for user: {} in category: {}", userId, category);
            return getDefaultCategoryVector(category);
        }

        // 4. 벡터 평균 계산
        return vectorProcessingService.calculateAverageVector(tagVectors);
    }



    private String getDefaultCategoryVector(MainCategory category) {
        return userCategoryVectorRepository.getDefaultCategoryVector(category);
    }
}
