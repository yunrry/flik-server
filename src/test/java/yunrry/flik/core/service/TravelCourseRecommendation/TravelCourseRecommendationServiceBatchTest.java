package yunrry.flik.core.service.TravelCourseRecommendation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import yunrry.flik.core.domain.mapper.CategoryMapper;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.service.plan.TravelCourseRecommendationService;
import yunrry.flik.core.service.plan.TravelPlannerService;
import yunrry.flik.core.service.plan.VectorSimilarityRecommendationService;
import yunrry.flik.core.service.user.UserPreferenceService;
import yunrry.flik.ports.out.repository.SpotRepository;
import yunrry.flik.ports.out.repository.UserSavedSpotRepository;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TravelCourseRecommendationServiceBatchTest {

    @InjectMocks
    private TravelCourseRecommendationService service;

    @Mock
    private TravelPlannerService travelPlannerService;

    @Mock
    private VectorSimilarityRecommendationService vectorSimilarityRecommendationService;

    @Mock
    private UserSavedSpotRepository userSavedSpotRepository;

    @Mock
    private UserPreferenceService userPreferenceService;

    @Mock
    private SpotRepository spotRepository;

    @Mock
    private CategoryMapper categoryMapper;

    private Long userId;
    private String regionCode;

    @BeforeEach
    void setUp() {
        userId = 1L;
        regionCode = "11";
    }

    @Test
    @DisplayName("TC-301: 필요한 카테고리 추출")
    void testExtractRequiredCategories() throws Exception {
        // Given
        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "history_culture", "restaurant", "accommodation"}
        };

        // When
        Set<MainCategory> result = invokeExtractRequiredCategories(courseStructure);

        // Then
        assertNotNull(result, "카테고리 Set이 null이 아니어야 함");
        assertEquals(5, result.size(), "5개 카테고리");
        assertTrue(result.contains(MainCategory.CAFE), "카페 포함");
        assertTrue(result.contains(MainCategory.NATURE), "자연 포함");
        assertTrue(result.contains(MainCategory.HISTORY_CULTURE), "문화 포함");
        assertTrue(result.contains(MainCategory.RESTAURANT), "식당 포함");
        assertTrue(result.contains(MainCategory.ACCOMMODATION), "숙박 포함");
    }

    @Test
    @DisplayName("TC-302: 카테고리별 일괄 조회 - DB 조회 횟수 확인")
    void testPreloadAllCategorySpotsQueryCount() throws Exception {
        // Given
        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "history_culture", "restaurant", "accommodation"},
                {"cafe", "nature", "restaurant", "history_culture", "restaurant", "accommodation"}
        };

        setupMocks();

        // When
        Map<MainCategory, List<Long>> result = invokePreloadAllCategorySpots(courseStructure);

        // Then
        assertNotNull(result, "캐시 맵이 null이 아니어야 함");
        assertEquals(5, result.size(), "5개 카테고리");

        // DB 조회 횟수 검증 (각 카테고리당 1번씩만)
        verify(spotRepository, times(5)).findIdsByIdsAndLabelDepth2InAndRegnCd(
                anyList(), anyList(), eq(regionCode));
    }

    @Test
    @DisplayName("TC-303: 빈 코스 구조 - 기본 카테고리만 추출")
    void testExtractRequiredCategoriesEmpty() throws Exception {
        // Given
        String[][] courseStructure = {
                {"", "", "", "", "", ""}
        };

        // When
        Set<MainCategory> result = invokeExtractRequiredCategories(courseStructure);

        // Then
        assertEquals(2, result.size(), "식당, 숙박 2개");
        assertTrue(result.contains(MainCategory.RESTAURANT), "식당 포함");
        assertTrue(result.contains(MainCategory.ACCOMMODATION), "숙박 포함");
    }

    @Test
    @DisplayName("TC-304: 중복 카테고리 제거")
    void testExtractRequiredCategoriesDeduplication() throws Exception {
        // Given
        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "nature", "restaurant", "accommodation"},
                {"cafe", "nature", "restaurant", "nature", "restaurant", "accommodation"},
                {"cafe", "nature", "restaurant", "nature", "restaurant", ""}
        };

        // When
        Set<MainCategory> result = invokeExtractRequiredCategories(courseStructure);

        // Then
        assertEquals(4, result.size(), "중복 제거 후 4개");
        assertTrue(result.contains(MainCategory.CAFE), "카페");
        assertTrue(result.contains(MainCategory.NATURE), "자연");
        assertTrue(result.contains(MainCategory.RESTAURANT), "식당");
        assertTrue(result.contains(MainCategory.ACCOMMODATION), "숙박");
    }

    @Test
    @DisplayName("TC-305: 일괄 조회 결과 검증")
    void testPreloadAllCategorySpotsResult() throws Exception {
        // Given
        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "history_culture", "restaurant", "accommodation"}
        };

        setupMocks();

        // When
        Map<MainCategory, List<Long>> result = invokePreloadAllCategorySpots(courseStructure);

        // Then
        assertTrue(result.containsKey(MainCategory.CAFE), "카페 키 존재");
        assertTrue(result.containsKey(MainCategory.NATURE), "자연 키 존재");
        assertTrue(result.containsKey(MainCategory.HISTORY_CULTURE), "문화 키 존재");
        assertTrue(result.containsKey(MainCategory.RESTAURANT), "식당 키 존재");
        assertTrue(result.containsKey(MainCategory.ACCOMMODATION), "숙박 키 존재");

        assertFalse(result.get(MainCategory.CAFE).isEmpty(), "카페 장소 있음");
        assertFalse(result.get(MainCategory.NATURE).isEmpty(), "자연 장소 있음");
        assertFalse(result.get(MainCategory.RESTAURANT).isEmpty(), "식당 장소 있음");
    }

    @Test
    @DisplayName("TC-306: 2박3일 코스 - DB 조회 최소화")
    void testPreloadThreeDaysMinimalQueries() throws Exception {
        // Given
        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "history_culture", "restaurant", "accommodation"},
                {"cafe", "market", "restaurant", "nature", "restaurant", "accommodation"},
                {"cafe", "history_culture", "restaurant", "market", "restaurant", ""}
        };

        setupMocks();

        // When
        Map<MainCategory, List<Long>> result = invokePreloadAllCategorySpots(courseStructure);

        // Then
        // 6개 카테고리 (cafe, nature, history_culture, market, restaurant, accommodation)
        assertEquals(6, result.size(), "6개 카테고리");

        // DB 조회는 6번만 (각 카테고리당 1번)
        verify(spotRepository, times(6)).findIdsByIdsAndLabelDepth2InAndRegnCd(
                anyList(), anyList(), eq(regionCode));
    }

    @Test
    @DisplayName("TC-307: 특별 카테고리 포함 - 일괄 조회")
    void testPreloadWithSpecialCategories() throws Exception {
        // Given
        String[][] courseStructure = {
                {"cafe", "activity", "restaurant", "festival", "restaurant", "accommodation"}
        };

        setupMocks();

        // When
        Map<MainCategory, List<Long>> result = invokePreloadAllCategorySpots(courseStructure);

        // Then
        assertEquals(5, result.size(), "5개 카테고리");
        assertTrue(result.containsKey(MainCategory.ACTIVITY), "액티비티 포함");
        assertTrue(result.containsKey(MainCategory.FESTIVAL), "축제 포함");
    }

    @Test
    @DisplayName("TC-308: 캐시 데이터로 중복 조회 방지")
    void testNoDuplicateQueriesWithCache() throws Exception {
        // Given
        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "nature", "restaurant", "accommodation"}
        };

        setupMocks();

        // When
        Map<MainCategory, List<Long>> cache = invokePreloadAllCategorySpots(courseStructure);

        // 동일한 캐시로 여러 번 사용해도 추가 DB 조회 없음
        List<Long> natureSpots1 = cache.get(MainCategory.NATURE);
        List<Long> natureSpots2 = cache.get(MainCategory.NATURE);

        // Then
        assertSame(natureSpots1, natureSpots2, "같은 인스턴스");

        // DB는 여전히 초기 조회만
        verify(spotRepository, times(4)).findIdsByIdsAndLabelDepth2InAndRegnCd(
                anyList(), anyList(), eq(regionCode));
    }

    @Test
    @DisplayName("TC-309: 카테고리별 장소 개수 확인")
    void testPreloadSpotCounts() throws Exception {
        // Given
        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "history_culture", "restaurant", "accommodation"}
        };

        when(userSavedSpotRepository.findSpotIdsByUserId(userId))
                .thenReturn(Arrays.asList(1L, 2L, 3L));

        when(categoryMapper.getSubCategoryNames(any(MainCategory.class)))
                .thenReturn(Arrays.asList("sub1", "sub2"));

        when(spotRepository.findIdsByIdsAndLabelDepth2InAndRegnCd(anyList(), anyList(), eq(regionCode)))
                .thenAnswer(invocation -> {
                    List<String> subCategories = invocation.getArgument(1);
                    if (subCategories.contains("sub1")) {
                        return Arrays.asList(101L, 102L, 103L, 104L, 105L);
                    }
                    return Arrays.asList(201L, 202L, 203L);
                });

        // When
        Map<MainCategory, List<Long>> result = invokePreloadAllCategorySpots(courseStructure);

        // Then
        assertTrue(result.values().stream().allMatch(list -> !list.isEmpty()),
                "모든 카테고리에 장소 있음");
    }

    @Test
    @DisplayName("TC-310: 빈 장소 목록 처리")
    void testPreloadEmptySpotList() throws Exception {
        // Given
        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "history_culture", "restaurant", "accommodation"}
        };

        when(userSavedSpotRepository.findSpotIdsByUserId(userId))
                .thenReturn(Collections.emptyList());

        when(categoryMapper.getSubCategoryNames(any(MainCategory.class)))
                .thenReturn(Arrays.asList("sub1", "sub2"));

        when(spotRepository.findIdsByIdsAndLabelDepth2InAndRegnCd(anyList(), anyList(), eq(regionCode)))
                .thenReturn(Collections.emptyList());

        // When
        Map<MainCategory, List<Long>> result = invokePreloadAllCategorySpots(courseStructure);

        // Then
        assertNotNull(result, "결과 맵은 null이 아님");
        assertEquals(5, result.size(), "카테고리는 추출됨");
        assertTrue(result.values().stream().allMatch(List::isEmpty),
                "모든 장소 목록이 비어있음");
    }

    @Test
    @DisplayName("TC-311: 모든 MainCategory 타입 (관광1개 제외) 일괄 조회")
    void testPreloadAllMainCategories() throws Exception {
        // Given
        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "indoor", "restaurant", "accommodation"},
                {"cafe", "activity", "restaurant", "festival", "restaurant", "accommodation"},
                {"cafe", "market", "restaurant", "themepark", "restaurant", ""}
        };

        setupMocks();

        // When
        Map<MainCategory, List<Long>> result = invokePreloadAllCategorySpots(courseStructure);

        // Then
        assertEquals(9, result.size(), "10개 카테고리 ((8-1)개 관광 + 식당 + 숙박)");
        assertTrue(result.containsKey(MainCategory.CAFE));
        assertTrue(result.containsKey(MainCategory.NATURE));
        assertTrue(result.containsKey(MainCategory.INDOOR));
        assertTrue(result.containsKey(MainCategory.ACTIVITY));
        assertTrue(result.containsKey(MainCategory.FESTIVAL));
        assertTrue(result.containsKey(MainCategory.MARKET));
        assertTrue(result.containsKey(MainCategory.THEMEPARK));
        assertTrue(result.containsKey(MainCategory.RESTAURANT));
        assertTrue(result.containsKey(MainCategory.ACCOMMODATION));

        // DB 조회는 카테고리 개수만큼만
        verify(spotRepository, times(9)).findIdsByIdsAndLabelDepth2InAndRegnCd(
                anyList(), anyList(), eq(regionCode));
    }

    // ========== Helper Methods ==========

    private void setupMocks() {
        when(userSavedSpotRepository.findSpotIdsByUserId(userId))
                .thenReturn(Arrays.asList(1L, 2L, 3L, 4L, 5L));

        when(categoryMapper.getSubCategoryNames(any(MainCategory.class)))
                .thenReturn(Arrays.asList("sub1", "sub2"));

        when(spotRepository.findIdsByIdsAndLabelDepth2InAndRegnCd(
                anyList(), anyList(), eq(regionCode)))
                .thenAnswer(invocation -> Arrays.asList(101L, 102L, 103L, 104L, 105L));
    }

    /**
     * private 메서드 extractRequiredCategories 호출
     */
    private Set<MainCategory> invokeExtractRequiredCategories(String[][] courseStructure) throws Exception {
        Method method = TravelCourseRecommendationService.class
                .getDeclaredMethod("extractRequiredCategories", String[][].class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Set<MainCategory> result = (Set<MainCategory>) method.invoke(service, (Object) courseStructure);

        return result;
    }

    /**
     * private 메서드 preloadAllCategorySpots 호출
     */
    private Map<MainCategory, List<Long>> invokePreloadAllCategorySpots(String[][] courseStructure) throws Exception {
        Method method = TravelCourseRecommendationService.class
                .getDeclaredMethod("preloadAllCategorySpots", String[][].class, Long.class, String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<MainCategory, List<Long>> result = (Map<MainCategory, List<Long>>) method.invoke(
                service, courseStructure, userId, regionCode);

        return result;
    }
}