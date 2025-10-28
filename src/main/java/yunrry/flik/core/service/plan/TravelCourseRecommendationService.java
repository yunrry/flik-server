package yunrry.flik.core.service.plan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import yunrry.flik.core.domain.mapper.CategoryMapper;
import yunrry.flik.core.domain.model.*;
import yunrry.flik.core.domain.model.plan.CourseSlot;
import yunrry.flik.core.domain.model.plan.SlotType;
import yunrry.flik.core.domain.model.plan.TravelCourse;
import yunrry.flik.core.service.user.UserPreferenceService;
import yunrry.flik.ports.in.query.CourseQuery;
import yunrry.flik.ports.out.repository.SpotRepository;
import yunrry.flik.ports.out.repository.UserSavedSpotRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TravelCourseRecommendationService {

    private static final int DEFAULT_TOURIST_SPOT_LIMIT = 7;
    private static final int RESTAURANT_LIMIT = 10;
    private static final int ACCOMMODATION_LIMIT = 5;

    private final TravelPlannerService travelPlannerService;
    private final VectorSimilarityRecommendationService vectorSimilarityRecommendationService;
    private final UserSavedSpotRepository userSavedSpotRepository;
    private final UserPreferenceService userPreferenceService;
    private final SpotRepository spotRepository;
    private final CategoryMapper categoryMapper;

    /**
     * 사용자 맞춤 여행 코스 생성 (동기)
     */
    public TravelCourse generatePersonalizedTravelCourse(CourseQuery query) {
        long startTime = System.currentTimeMillis();

        String regionCode = query.getSelectedRegion();
        Long userId = query.getUserId();
        List<String> selectedCategoriesList = query.getSelectedCategories();
        String[] selectedCategories = selectedCategoriesList.toArray(new String[0]);
        int days = query.getDays();

        try {
            // 1. 사용자 선호도 조회
            Map<String, Integer> frequencyMap = getUserSelectFrequency(userId, selectedCategories);

            // 2. 기본 코스 구조 생성
            String[][] courseStructure = travelPlannerService.generateTravelCourse(
                    selectedCategories,
                    days,
                    frequencyMap
            );

            logCourseStructure(courseStructure);

            // 3. 필요한 모든 카테고리의 장소를 미리 조회 (N+1 해결)
            Map<MainCategory, List<Long>> categorySpotCache =
                    preloadAllCategorySpots(courseStructure, userId, regionCode);

            // 4. 각 슬롯에 실제 장소 할당
            CourseSlot[][] filledCourse = fillCourseWithRecommendedSpots(
                    userId,
                    courseStructure,
                    regionCode,
                    categorySpotCache
            );

            TravelCourse result = TravelCourse.of(
                    userId,
                    days,
                    filledCourse,
                    selectedCategoriesList,
                    regionCode
            );

            log.info("Generated personalized course for user: {} with {} days in {}ms",
                    userId, days, System.currentTimeMillis() - startTime);

            return result;

        } catch (Exception e) {
            log.error("Failed to generate course for user: {}", userId, e);
            throw new RuntimeException("코스 생성에 실패했습니다.", e);
        }
    }

    /**
     * 사용자 선호도 빈도 조회
     */
    private Map<String, Integer> getUserSelectFrequency(Long userId, String[] selectedCategories) {
        Map<String, Integer> frequencyMap = new HashMap<>();

        for (String category : selectedCategories) {
            MainCategory mainCategory = MainCategory.findByCode(category);
            int count = userPreferenceService.getUserMainCategoryCount(userId, mainCategory.getDisplayName());
            frequencyMap.put(category, count > 0 ? count : 1);
        }

        return frequencyMap;
    }

    /**
     * 필요한 모든 카테고리의 장소를 미리 조회 (N+1 해결)
     */
    private Map<MainCategory, List<Long>> preloadAllCategorySpots(
            String[][] courseStructure,
            Long userId,
            String regionCode) {

        // 1. 코스에 필요한 모든 카테고리 추출
        Set<MainCategory> requiredCategories = extractRequiredCategories(courseStructure);

        log.debug("Preloading spots for categories: {}", requiredCategories);

        // 2. 카테고리별로 한 번에 조회
        Map<MainCategory, List<Long>> categorySpotMap = new HashMap<>();

        for (MainCategory category : requiredCategories) {
            List<Long> spotIds = getCategorySpotsWithCache(userId, category, regionCode);
            categorySpotMap.put(category, spotIds);

            log.debug("Loaded {} spots for category: {}", spotIds.size(), category);
        }

        return categorySpotMap;
    }

    /**
     * 코스 구조에서 필요한 카테고리 추출
     */
    private Set<MainCategory> extractRequiredCategories(String[][] courseStructure) {
        Set<MainCategory> categories = new HashSet<>();

        // 필수 카테고리
        categories.add(MainCategory.RESTAURANT);
        categories.add(MainCategory.ACCOMMODATION);

        // 관광 카테고리
        for (String[] day : courseStructure) {
            for (String slot : day) {
                if (!slot.isEmpty() &&
                        !slot.equals("restaurant") &&
                        !slot.equals("accommodation")) {

                    MainCategory category = MainCategory.findByCode(slot);
                    if (category != null) {
                        categories.add(category);
                    }
                }
            }
        }

        return categories;
    }

    /**
     * 캐싱을 적용한 카테고리별 장소 조회
     * Redis 캐시 사용 (TTL: 10분)
     */
    @Cacheable(
            value = "categorySpots",
            key = "#userId + ':' + #category.code + ':' + #regionCode",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<Long> getCategorySpotsWithCache(
            Long userId,
            MainCategory category,
            String regionCode) {

        log.debug("Cache miss - Loading spots from DB for userId: {}, category: {}, region: {}",
                userId, category.getCode(), regionCode);

        List<String> subCategories = categoryMapper.getSubCategoryNames(category);
        List<Long> userSavedSpotIds = userSavedSpotRepository.findSpotIdsByUserId(userId);

        List<Long> spotIds = spotRepository.findIdsByIdsAndLabelDepth2InAndRegnCd(
                userSavedSpotIds,
                subCategories,
                regionCode
        );

        log.debug("Loaded {} spots for category: {} from DB", spotIds.size(), category.getCode());

        return spotIds;
    }

    /**
     * 코스에 추천 장소 채우기
     */
    public CourseSlot[][] fillCourseWithRecommendedSpots(
            Long userId,
            String[][] courseStructure,
            String regionCode,
            Map<MainCategory, List<Long>> categorySpotCache) {

        CourseSlot[][] filledCourse = new CourseSlot[courseStructure.length][courseStructure[0].length];

        for (int day = 0; day < courseStructure.length; day++) {
            for (int slot = 0; slot < courseStructure[day].length; slot++) {
                String slotCategory = courseStructure[day][slot];
                System.out.println("day"+day+"slot"+slot+" = "+courseStructure[day][slot]);
                if (slotCategory.isEmpty()) {
                    filledCourse[day][slot] = CourseSlot.empty(day, slot);
                    System.out.println("filledCourse[day][slot]="+filledCourse[day][slot]+ "-> is empty");
                } else {
                    CourseSlot courseSlot = createCourseSlot(
                            userId,
                            day,
                            slot,
                            slotCategory,
                            regionCode,
                            categorySpotCache
                    );
                    filledCourse[day][slot] = courseSlot;
                }
            }
        }

        return filledCourse;
    }

    /**
     * 슬롯 타입에 따라 CourseSlot 생성
     */
    private CourseSlot createCourseSlot(
            Long userId,
            int day,
            int slot,
            String slotCategory,
            String regionCode,
            Map<MainCategory, List<Long>> categorySpotCache) {

        if ("restaurant".equals(slotCategory)) {
            return createRestaurantSlot(userId, day, slot, regionCode, categorySpotCache);
        }

        if ("accommodation".equals(slotCategory)) {
            return createAccommodationSlot(userId, day, slot, regionCode, categorySpotCache);
        }

        // 관광 슬롯 처리
        MainCategory mainCategory = getMainCategoryFromSlotType(slotCategory);

        if (mainCategory != null) {
            List<Long> candidateSpotIds = categorySpotCache.getOrDefault(
                    mainCategory,
                    Collections.emptyList()
            );

            if (candidateSpotIds.isEmpty()) {
                log.warn("No candidate spots found for category: {} in region: {}",
                        mainCategory, regionCode);
                return CourseSlot.empty(day, slot);
            }

            List<Long> recommendedSpotIds = vectorSimilarityRecommendationService
                    .findRecommendedSpotIdsByVectorSimilarity(
                            userId,
                            candidateSpotIds,
                            mainCategory,
                            DEFAULT_TOURIST_SPOT_LIMIT
                    );

            log.debug("Recommended {} spots for user {} in category {}",
                    recommendedSpotIds.size(), userId, mainCategory.getCode());
            System.out.println("Recommended "+recommendedSpotIds.size()+" in category "+mainCategory.getCode());

            return CourseSlot.builder()
                    .day(day + 1)
                    .slot(slot)
                    .slotType(SlotType.fromMainCategory(mainCategory))
                    .mainCategory(mainCategory)
                    .recommendedSpotIds(recommendedSpotIds)
                    .build();
        }

        return CourseSlot.empty(day, slot);
    }

    /**
     * 식당 슬롯 생성
     */
    private CourseSlot createRestaurantSlot(
            Long userId,
            int day,
            int slot,
            String regionCode,
            Map<MainCategory, List<Long>> categorySpotCache) {

        List<Long> candidateSpotIds = categorySpotCache.getOrDefault(
                MainCategory.RESTAURANT,
                Collections.emptyList()
        );

        if (candidateSpotIds.isEmpty()) {
            log.warn("No restaurant spots found in region: {}", regionCode);
            return CourseSlot.empty(day, slot);
        }

        List<Long> recommendedSpotIds = vectorSimilarityRecommendationService
                .findRecommendedSpotIdsByVectorSimilarity(
                        userId,
                        candidateSpotIds,
                        MainCategory.RESTAURANT,
                        RESTAURANT_LIMIT
                );

        log.debug("Recommended {} restaurants for user {}", recommendedSpotIds.size(), userId);

        return CourseSlot.builder()
                .day(day + 1)
                .slot(slot)
                .slotType(SlotType.RESTAURANT)
                .mainCategory(MainCategory.RESTAURANT)
                .recommendedSpotIds(recommendedSpotIds)
                .build();
    }

    /**
     * 숙박 슬롯 생성
     */
    private CourseSlot createAccommodationSlot(
            Long userId,
            int day,
            int slot,
            String regionCode,
            Map<MainCategory, List<Long>> categorySpotCache) {

        List<Long> candidateSpotIds = categorySpotCache.getOrDefault(
                MainCategory.ACCOMMODATION,
                Collections.emptyList()
        );

        if (candidateSpotIds.isEmpty()) {
            log.warn("No accommodation spots found in region: {}", regionCode);
            return CourseSlot.empty(day, slot);
        }

        List<Long> recommendedSpotIds = vectorSimilarityRecommendationService
                .findRecommendedSpotIdsByVectorSimilarity(
                        userId,
                        candidateSpotIds,
                        MainCategory.ACCOMMODATION,
                        ACCOMMODATION_LIMIT
                );

        log.debug("Recommended {} accommodations for user {}", recommendedSpotIds.size(), userId);

        return CourseSlot.builder()
                .day(day + 1)
                .slot(slot)
                .slotType(SlotType.ACCOMMODATION)
                .mainCategory(MainCategory.ACCOMMODATION)
                .recommendedSpotIds(recommendedSpotIds)
                .build();
    }

    /**
     * 슬롯 타입에서 메인 카테고리 추출
     */
    private MainCategory getMainCategoryFromSlotType(String slotType) {
        return MainCategory.findByCode(slotType);
    }

    /**
     * 코스 구조 로깅
     */
    private void logCourseStructure(String[][] courseStructure) {
        if (log.isDebugEnabled()) {
            log.debug("=== Course Structure ===");
            for (int i = 0; i < courseStructure.length; i++) {
                log.debug("Day {}: {}", i + 1, Arrays.toString(courseStructure[i]));
            }
        }
    }
}