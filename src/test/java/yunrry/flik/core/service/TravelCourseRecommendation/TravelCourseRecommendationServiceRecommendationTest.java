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
import yunrry.flik.core.domain.model.plan.CourseSlot;
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
class TravelCourseRecommendationServiceRecommendationTest {

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
    private Map<MainCategory, List<Long>> categorySpotCache;

    @BeforeEach
    void setUp() {
        userId = 1L;
        regionCode = "11";
        categorySpotCache = new HashMap<>();
    }

    @Test
    @DisplayName("TC-201: 관광지 추천 - 3개")
    void testRecommendTouristSpots() throws Exception {
        // Given
        List<Long> candidateSpotIds = Arrays.asList(301L, 302L, 303L, 304L, 305L);
        List<Long> recommendedIds = Arrays.asList(301L, 302L, 303L);

        categorySpotCache.put(MainCategory.NATURE, candidateSpotIds);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId), eq(candidateSpotIds), eq(MainCategory.NATURE), eq(7)))
                .thenReturn(recommendedIds);

        // When
        CourseSlot result = invokeCreateCourseSlot(userId, 0, 1, "nature");

        // Then
        assertNotNull(result, "슬롯이 생성되어야 함");
        assertEquals(MainCategory.NATURE, result.getMainCategory(), "자연 카테고리");
        assertEquals(3, result.getRecommendedSpotIds().size(), "7개 추천");
        assertEquals(recommendedIds, result.getRecommendedSpotIds(), "추천 ID 일치");

        verify(vectorSimilarityRecommendationService).findRecommendedSpotIdsByVectorSimilarity(
                userId, candidateSpotIds, MainCategory.NATURE, 7);
    }

    @Test
    @DisplayName("TC-202: 식당 추천 - 10개")
    void testRecommendRestaurants() throws Exception {
        // Given
        List<Long> candidateSpotIds = Arrays.asList(101L, 102L, 103L, 104L, 105L,
                106L, 107L, 108L, 109L, 110L, 111L);
        List<Long> recommendedIds = Arrays.asList(101L, 102L, 103L, 104L, 105L,
                106L, 107L, 108L, 109L, 110L);

        categorySpotCache.put(MainCategory.RESTAURANT, candidateSpotIds);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId), eq(candidateSpotIds), eq(MainCategory.RESTAURANT), eq(10)))
                .thenReturn(recommendedIds);

        // When
        CourseSlot result = invokeCreateCourseSlot(userId, 0, 2, "restaurant");

        // Then
        assertNotNull(result, "슬롯이 생성되어야 함");
        assertEquals(MainCategory.RESTAURANT, result.getMainCategory(), "식당 카테고리");
        assertEquals(10, result.getRecommendedSpotIds().size(), "10개 추천");
        assertEquals(recommendedIds, result.getRecommendedSpotIds(), "추천 ID 일치");

        verify(vectorSimilarityRecommendationService).findRecommendedSpotIdsByVectorSimilarity(
                userId, candidateSpotIds, MainCategory.RESTAURANT, 10);
    }

    @Test
    @DisplayName("TC-203: 숙박 추천 - 5개")
    void testRecommendAccommodations() throws Exception {
        // Given
        List<Long> candidateSpotIds = Arrays.asList(201L, 202L, 203L, 204L, 205L, 206L);
        List<Long> recommendedIds = Arrays.asList(201L, 202L, 203L, 204L, 205L);

        categorySpotCache.put(MainCategory.ACCOMMODATION, candidateSpotIds);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId), eq(candidateSpotIds), eq(MainCategory.ACCOMMODATION), eq(5)))
                .thenReturn(recommendedIds);

        // When
        CourseSlot result = invokeCreateCourseSlot(userId, 0, 5, "accommodation");

        // Then
        assertNotNull(result, "슬롯이 생성되어야 함");
        assertEquals(MainCategory.ACCOMMODATION, result.getMainCategory(), "숙박 카테고리");
        assertEquals(5, result.getRecommendedSpotIds().size(), "5개 추천");
        assertEquals(recommendedIds, result.getRecommendedSpotIds(), "추천 ID 일치");

        verify(vectorSimilarityRecommendationService).findRecommendedSpotIdsByVectorSimilarity(
                userId, candidateSpotIds, MainCategory.ACCOMMODATION, 5);
    }

    @Test
    @DisplayName("TC-204: 후보 장소 없음 - 빈 슬롯 반환")
    void testRecommendWithNoCandidates() throws Exception {
        // Given
        categorySpotCache.put(MainCategory.NATURE, Collections.emptyList());

        // When
        CourseSlot result = invokeCreateCourseSlot(userId, 0, 1, "nature");

        // Then
        assertNotNull(result, "슬롯이 생성되어야 함");
        assertTrue(result.getRecommendedSpotIds().isEmpty(), "추천 없음");

        verify(vectorSimilarityRecommendationService, never())
                .findRecommendedSpotIdsByVectorSimilarity(anyLong(), anyList(), any(), anyInt());
    }

    @Test
    @DisplayName("TC-205: 다양한 카테고리 추천")
    void testRecommendVariousCategories() throws Exception {
        // Given
        Map<MainCategory, List<Long>> candidates = new HashMap<>();
        candidates.put(MainCategory.CAFE, Arrays.asList(401L, 402L, 403L));
        candidates.put(MainCategory.HISTORY_CULTURE, Arrays.asList(501L, 502L, 503L));
        candidates.put(MainCategory.MARKET, Arrays.asList(601L, 602L, 603L));

        categorySpotCache.putAll(candidates);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId), anyList(), any(MainCategory.class), eq(7)))
                .thenAnswer(invocation -> {
                    MainCategory category = invocation.getArgument(2);
                    return candidates.get(category);
                });

        // When
        CourseSlot cafeSlot = invokeCreateCourseSlot(userId, 0, 0, "cafe");
        CourseSlot cultureSlot = invokeCreateCourseSlot(userId, 0, 1, "history_culture");
        CourseSlot marketSlot = invokeCreateCourseSlot(userId, 0, 7, "market");

        // Then
        assertEquals(MainCategory.CAFE, cafeSlot.getMainCategory());
        assertEquals(3, cafeSlot.getRecommendedSpotIds().size());

        assertEquals(MainCategory.HISTORY_CULTURE, cultureSlot.getMainCategory());
        assertEquals(3, cultureSlot.getRecommendedSpotIds().size());

        assertEquals(MainCategory.MARKET, marketSlot.getMainCategory());
        assertEquals(3, marketSlot.getRecommendedSpotIds().size());
    }

    @Test
    @DisplayName("TC-206: 후보보다 적은 추천")
    void testRecommendLessThanCandidates() throws Exception {
        // Given
        List<Long> candidateSpotIds = Arrays.asList(301L, 302L);
        List<Long> recommendedIds = Arrays.asList(301L, 302L);

        categorySpotCache.put(MainCategory.NATURE, candidateSpotIds);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId), eq(candidateSpotIds), eq(MainCategory.NATURE), eq(7)))
                .thenReturn(recommendedIds);

        // When
        CourseSlot result = invokeCreateCourseSlot(userId, 0, 1, "nature");

        // Then
        assertEquals(2, result.getRecommendedSpotIds().size(), "후보가 적으면 적은 개수 반환");
    }

    @Test
    @DisplayName("TC-207: 카페 슬롯 추천")
    void testRecommendCafe() throws Exception {
        // Given
        List<Long> candidateSpotIds = Arrays.asList(401L, 402L, 403L, 404L);
        List<Long> recommendedIds = Arrays.asList(401L, 402L, 403L);

        categorySpotCache.put(MainCategory.CAFE, candidateSpotIds);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId), eq(candidateSpotIds), eq(MainCategory.CAFE), eq(7)))
                .thenReturn(recommendedIds);

        // When
        CourseSlot result = invokeCreateCourseSlot(userId, 0, 0, "cafe");

        // Then
        assertEquals(MainCategory.CAFE, result.getMainCategory(), "카페 카테고리");
        assertEquals(3, result.getRecommendedSpotIds().size(), "3개 추천");
    }

    @Test
    @DisplayName("TC-208: 특별 카테고리 추천 (액티비티)")
    void testRecommendActivity() throws Exception {
        // Given
        List<Long> candidateSpotIds = Arrays.asList(701L, 702L, 703L, 704L);
        List<Long> recommendedIds = Arrays.asList(701L, 702L, 703L);

        categorySpotCache.put(MainCategory.ACTIVITY, candidateSpotIds);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId), eq(candidateSpotIds), eq(MainCategory.ACTIVITY), eq(7)))
                .thenReturn(recommendedIds);

        // When
        CourseSlot result = invokeCreateCourseSlot(userId, 0, 1, "activity");

        // Then
        assertEquals(MainCategory.ACTIVITY, result.getMainCategory(), "액티비티 카테고리");
        assertEquals(3, result.getRecommendedSpotIds().size(), "3개 추천");
    }

    @Test
    @DisplayName("TC-209: 특별 카테고리 추천 (축제)")
    void testRecommendFestival() throws Exception {
        // Given
        List<Long> candidateSpotIds = Arrays.asList(801L, 802L, 803L);
        List<Long> recommendedIds = Arrays.asList(801L, 802L, 803L);

        categorySpotCache.put(MainCategory.FESTIVAL, candidateSpotIds);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId), eq(candidateSpotIds), eq(MainCategory.FESTIVAL), eq(7)))
                .thenReturn(recommendedIds);

        // When
        CourseSlot result = invokeCreateCourseSlot(userId, 0, 1, "festival");

        // Then
        assertEquals(MainCategory.FESTIVAL, result.getMainCategory(), "축제 카테고리");
        assertEquals(3, result.getRecommendedSpotIds().size(), "3개 추천");
    }

    @Test
    @DisplayName("TC-210: 슬롯별 day, slot 인덱스 정확성")
    void testSlotIndexAccuracy() throws Exception {
        // Given
        List<Long> candidateSpotIds = Arrays.asList(301L, 302L, 303L);
        List<Long> recommendedIds = Arrays.asList(301L, 302L, 303L);

        categorySpotCache.put(MainCategory.NATURE, candidateSpotIds);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                anyLong(), anyList(), any(), anyInt()))
                .thenReturn(recommendedIds);

        // When
        CourseSlot slot1 = invokeCreateCourseSlot(userId, 0, 1, "nature");
        CourseSlot slot2 = invokeCreateCourseSlot(userId, 1, 7, "nature");
        CourseSlot slot3 = invokeCreateCourseSlot(userId, 2, 5, "nature");

        // Then
        assertEquals(1, slot1.getDay(), "DAY 1");
        assertEquals(1, slot1.getSlot(), "슬롯 1");

        assertEquals(2, slot2.getDay(), "DAY 2");
        assertEquals(7, slot2.getSlot(), "슬롯 3");

        assertEquals(3, slot3.getDay(), "DAY 3");
        assertEquals(5, slot3.getSlot(), "슬롯 5");
    }

    // ========== Helper Methods ==========

    /**
     * private 메서드 createCourseSlot 호출
     */
    private CourseSlot invokeCreateCourseSlot(
            Long userId, int day, int slot, String slotType) throws Exception {

        Method method = TravelCourseRecommendationService.class.getDeclaredMethod(
                "createCourseSlot",
                Long.class, int.class, int.class, String.class, String.class, Map.class);
        method.setAccessible(true);

        return (CourseSlot) method.invoke(
                service, userId, day, slot, slotType, regionCode, categorySpotCache);
    }
}