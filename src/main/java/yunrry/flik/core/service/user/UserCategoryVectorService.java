package yunrry.flik.core.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yunrry.flik.core.domain.mapper.CategoryMapper;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.embedding.UserVectorStats;
import yunrry.flik.ports.out.repository.SpotRepository;
import yunrry.flik.ports.out.repository.UserCategoryVectorRepository;
import yunrry.flik.ports.out.repository.UserSavedSpotRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCategoryVectorService {

    private final UserCategoryVectorRepository userCategoryVectorRepository;
    private final UserSavedSpotRepository userSavedSpotRepository;
    private final SpotRepository spotRepository;
    private final CategoryMapper categoryMapper;

    /**
     * 사용자 카테고리 선호 벡터 조회 (캐시 적용)
     */
    @Cacheable(value = "userCategoryVectors", key = "#userId + '_' + #category.code + '_' + #categorySavedCount")
    public Optional<List<Double>> getUserCategoryPreferenceVector(Long userId, MainCategory category, int categorySavedCount) {
        // 캐시에 없으면 벡터 업데이트 후 조회
        updateUserPreferenceVectorIfNeeded(userId, category);
        return userCategoryVectorRepository.getUserCategoryVector(userId, category);
    }

    /**
     * 필요시 벡터 업데이트
     */
    private void updateUserPreferenceVectorIfNeeded(Long userId, MainCategory category) {
        List<Long> allSavedSpotIds = userSavedSpotRepository.findSpotIdsByUserId(userId);
        if (!allSavedSpotIds.isEmpty()) {
            recalculateCategoryVector(userId, category, allSavedSpotIds);
        }
    }


    /**
     * PostgreSQL 함수를 사용한 선호도 벡터 업데이트
     * 사용자가 새로운 장소를 저장했을 때 호출
     */
    @CacheEvict(value = "userCategoryVectors", key = "#userId + '_' + #category.code")
    public void updateUserPreferenceVector(Long userId, MainCategory category, List<Long> newFavoriteSpotIds) {
        try {
            userCategoryVectorRepository.updateUserPreferenceVector(userId, category, newFavoriteSpotIds);
        } catch (Exception e) {
            log.error("Failed to update User category vector: {}", e.getMessage());
        }
    }

    /**
     * 사용자의 모든 저장된 장소를 기반으로 전체 벡터 재계산
     */
    @CacheEvict(value = "userCategoryVectors", allEntries = true, condition = "#userId != null")
    public void recalculateAllUserVectors(Long userId) {
        List<Long> allSavedSpotIds = userSavedSpotRepository.findSpotIdsByUserId(userId);

        if (allSavedSpotIds.isEmpty()) {
            log.debug("No saved spots for user: {}", userId);
            return;
        }

        // 각 카테고리별로 벡터 재계산
        for (MainCategory category : MainCategory.values()) {
            List<Long> categorySpots = filterSpotsByCategory(allSavedSpotIds, category);
            recalculateCategoryVector(userId, category, categorySpots);
        }
    }

    private List<Long> filterSpotsByCategory(List<Long> spotIds, MainCategory category) {
        List<String> subCategories = categoryMapper.getSubCategoryNames(category);

        return spotRepository.findIdsByIdsAndLabelDepth2In(spotIds, subCategories);
    }


    /**
     * 특정 카테고리 벡터 재계산
     */
    @CacheEvict(value = "userCategoryVectors", key = "#userId + '_' + #category.code")
    public void recalculateCategoryVector(Long userId, MainCategory category, List<Long> allSpotIds) {


        try {
            userCategoryVectorRepository.recalculateCategoryVector(userId, category, allSpotIds);
            log.debug("Recalculated category vector for user: {}, category: {}, spots: {}", userId, category, allSpotIds.size());
        } catch (Exception e) {
            log.error("Failed to recalculate category vector: {}", e.getMessage());
        }
    }



    /**
     * 기본 벡터 반환
     */
    public List<Double> getDefaultCategoryVector(MainCategory category) {
        return userCategoryVectorRepository.getDefaultCategoryVector(category);
    }

    /**
     * 사용자 벡터 통계 조회
     */
    public UserVectorStats getUserVectorStats(Long userId) {
        return userCategoryVectorRepository.getUserVectorStats(userId);
    }

    /**
     * 벡터 존재 여부 확인
     */
    public boolean hasUserVector(Long userId, MainCategory category) {
        return userCategoryVectorRepository.existsByUserIdAndCategory(userId, category);
    }



}