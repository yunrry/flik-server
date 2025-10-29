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
import yunrry.flik.core.domain.model.plan.SlotType;
import yunrry.flik.core.service.plan.TravelCourseRecommendationService;
import yunrry.flik.core.service.plan.TravelPlannerService;
import yunrry.flik.core.service.plan.VectorSimilarityRecommendationService;
import yunrry.flik.core.service.user.UserPreferenceService;
import yunrry.flik.ports.out.repository.SpotRepository;
import yunrry.flik.ports.out.repository.UserSavedSpotRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TravelCourseRecommendationServiceSlotTest {

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

        // 기본 카테고리 캐시 데이터
        categorySpotCache.put(MainCategory.RESTAURANT, Arrays.asList(101L, 102L, 103L));
        categorySpotCache.put(MainCategory.ACCOMMODATION, Arrays.asList(201L, 202L));
        categorySpotCache.put(MainCategory.NATURE, Arrays.asList(301L, 302L, 303L));
        categorySpotCache.put(MainCategory.CAFE, Arrays.asList(401L, 402L));
    }

    @Test
    @DisplayName("TC-501: 빈 슬롯 생성")
    void testCreateEmptySlot() {
        // Given
        String[][] courseStructure = {
                {"", "", "", "", "", ""}
        };

        // When
        CourseSlot[][] result = invokePrivateFillCourse(courseStructure);

        // Then
        CourseSlot slot = result[0][0];
        assertTrue(slot.getRecommendedSpotIds().isEmpty(), "빈 슬롯은 추천 장소가 없어야 함");
        assertNull(slot.getMainCategory(), "빈 슬롯은 카테고리가 null");
    }

    @Test
    @DisplayName("TC-502: 식당 슬롯 생성")
    void testCreateRestaurantSlot() {
        // Given
        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "nature", "restaurant", "accommodation"}
        };

        List<Long> restaurantIds = Arrays.asList(101L, 102L, 103L, 104L, 105L,
                106L, 107L, 108L, 109L, 110L);
        List<Long> cafeIds = Arrays.asList(401L, 402L, 403L);
        List<Long> natureIds = Arrays.asList(301L, 302L, 303L);
        List<Long> accommodationIds = Arrays.asList(201L, 202L, 203L, 204L, 205L);

        // Mock 설정 - 각 카테고리별로 정확한 매개변수 매칭
        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId),
                eq(categorySpotCache.get(MainCategory.CAFE)),
                eq(MainCategory.CAFE),
                eq(7)
        )).thenReturn(cafeIds);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId),
                eq(categorySpotCache.get(MainCategory.NATURE)),
                eq(MainCategory.NATURE),
                eq(7)
        )).thenReturn(natureIds);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId),
                eq(categorySpotCache.get(MainCategory.RESTAURANT)),
                eq(MainCategory.RESTAURANT),
                eq(10)
        )).thenReturn(restaurantIds);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId),
                eq(categorySpotCache.get(MainCategory.ACCOMMODATION)),
                eq(MainCategory.ACCOMMODATION),
                eq(5)
        )).thenReturn(accommodationIds);

        // When
        CourseSlot[][] result = invokePrivateFillCourse(courseStructure);

        // Then
        CourseSlot restaurantSlot1 = result[0][2]; // 점심 슬롯
        assertEquals(SlotType.RESTAURANT, restaurantSlot1.getSlotType(), "식당 타입");
        assertEquals(MainCategory.RESTAURANT, restaurantSlot1.getMainCategory(), "식당 카테고리");
        assertEquals(10, restaurantSlot1.getRecommendedSpotIds().size(), "식당 10개 추천");

        CourseSlot restaurantSlot2 = result[0][4]; // 저녁 슬롯
        assertEquals(SlotType.RESTAURANT, restaurantSlot2.getSlotType(), "식당 타입");

        verify(vectorSimilarityRecommendationService, times(2))
                .findRecommendedSpotIdsByVectorSimilarity(
                        eq(userId),
                        eq(categorySpotCache.get(MainCategory.RESTAURANT)),
                        eq(MainCategory.RESTAURANT),
                        eq(10)
                );
    }

    @Test
    @DisplayName("TC-503: 숙박 슬롯 생성")
    void testCreateAccommodationSlot() {
        // Given
        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "nature", "restaurant", "accommodation"}
        };

        List<Long> accommodationIds = Arrays.asList(201L, 202L, 203L, 204L, 205L);
        List<Long> cafeIds = Arrays.asList(401L, 402L);
        List<Long> natureIds = Arrays.asList(301L, 302L, 303L);
        List<Long> restaurantIds = Arrays.asList(101L, 102L, 103L, 104L, 105L,
                106L, 107L, 108L, 109L, 110L);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId),
                eq(categorySpotCache.get(MainCategory.CAFE)),
                eq(MainCategory.CAFE),
                eq(7)
        )).thenReturn(cafeIds);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId),
                eq(categorySpotCache.get(MainCategory.NATURE)),
                eq(MainCategory.NATURE),
                eq(7)
        )).thenReturn(natureIds);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId),
                eq(categorySpotCache.get(MainCategory.RESTAURANT)),
                eq(MainCategory.RESTAURANT),
                eq(10)
        )).thenReturn(restaurantIds);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId),
                eq(categorySpotCache.get(MainCategory.ACCOMMODATION)),
                eq(MainCategory.ACCOMMODATION),
                eq(5)
        )).thenReturn(accommodationIds);

        // When
        CourseSlot[][] result = invokePrivateFillCourse(courseStructure);

        // Then
        CourseSlot accommodationSlot = result[0][5]; // 숙박 슬롯
        assertEquals(SlotType.ACCOMMODATION, accommodationSlot.getSlotType(), "숙박 타입");
        assertEquals(MainCategory.ACCOMMODATION, accommodationSlot.getMainCategory(), "숙박 카테고리");
        assertEquals(5, accommodationSlot.getRecommendedSpotIds().size(), "숙박 5개 추천");

        verify(vectorSimilarityRecommendationService).findRecommendedSpotIdsByVectorSimilarity(
                eq(userId),
                eq(categorySpotCache.get(MainCategory.ACCOMMODATION)),
                eq(MainCategory.ACCOMMODATION),
                eq(5)
        );
    }

    @Test
    @DisplayName("TC-504: 관광 슬롯 생성")
    void testCreateTouristSlot() {
        // Given
        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "history_culture", "restaurant", "accommodation"}
        };

        List<Long> cafeIds = Arrays.asList(401L, 402L, 403L);
        List<Long> natureIds = Arrays.asList(301L, 302L, 303L);
        List<Long> historyIds = Arrays.asList(501L, 502L, 503L);
        List<Long> restaurantIds = Arrays.asList(101L, 102L, 103L, 104L, 105L,
                106L, 107L, 108L, 109L, 110L);
        List<Long> accommodationIds = Arrays.asList(201L, 202L, 203L, 204L, 205L);

        categorySpotCache.put(MainCategory.HISTORY_CULTURE, Arrays.asList(501L, 502L, 503L, 504L));

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId),
                eq(categorySpotCache.get(MainCategory.CAFE)),
                eq(MainCategory.CAFE),
                eq(7)
        )).thenReturn(cafeIds);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId),
                eq(categorySpotCache.get(MainCategory.NATURE)),
                eq(MainCategory.NATURE),
                eq(7)
        )).thenReturn(natureIds);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId),
                eq(categorySpotCache.get(MainCategory.HISTORY_CULTURE)),
                eq(MainCategory.HISTORY_CULTURE),
                eq(7)
        )).thenReturn(historyIds);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId),
                eq(categorySpotCache.get(MainCategory.RESTAURANT)),
                eq(MainCategory.RESTAURANT),
                eq(10)
        )).thenReturn(restaurantIds);

        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId),
                eq(categorySpotCache.get(MainCategory.ACCOMMODATION)),
                eq(MainCategory.ACCOMMODATION),
                eq(5)
        )).thenReturn(accommodationIds);

        // When
        CourseSlot[][] result = invokePrivateFillCourse(courseStructure);

        // Then - 자연 슬롯
        CourseSlot natureSlot = result[0][1];
        assertEquals(SlotType.fromMainCategory(MainCategory.NATURE), natureSlot.getSlotType());
        assertEquals(MainCategory.NATURE, natureSlot.getMainCategory());
        assertEquals(3, natureSlot.getRecommendedSpotIds().size(), "자연 3개 추천");

        // Then - 문화 슬롯
        CourseSlot historySlot = result[0][3];
        assertEquals(SlotType.fromMainCategory(MainCategory.HISTORY_CULTURE), historySlot.getSlotType());
        assertEquals(MainCategory.HISTORY_CULTURE, historySlot.getMainCategory());
        assertEquals(3, historySlot.getRecommendedSpotIds().size(), "문화 3개 추천");
    }

    @Test
    @DisplayName("TC-505: 후보 장소 없는 경우 빈 슬롯 반환")
    void testCreateSlotWithNoCandidates() {
        // Given
        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "nature", "restaurant", ""}
        };

        // 자연 카테고리 후보 없음
        categorySpotCache.put(MainCategory.NATURE, Collections.emptyList());

        // When
        CourseSlot[][] result = invokePrivateFillCourse(courseStructure);

        // Then
        CourseSlot slot1 = result[0][1];
        assertTrue(slot1.getRecommendedSpotIds().isEmpty(), "후보 없으면 빈 슬롯");

        CourseSlot slot3 = result[0][3];
        assertTrue(slot3.getRecommendedSpotIds().isEmpty(), "후보 없으면 빈 슬롯");

        verify(vectorSimilarityRecommendationService, never())
                .findRecommendedSpotIdsByVectorSimilarity(
                        anyLong(), anyList(), eq(MainCategory.NATURE), anyInt()
                );
    }

    /**
     * private 메서드를 테스트하기 위한 헬퍼 메서드
     */
    private CourseSlot[][] invokePrivateFillCourse(String[][] courseStructure) {
        try {
            var method = TravelCourseRecommendationService.class
                    .getDeclaredMethod("fillCourseWithRecommendedSpots",
                            Long.class, String[][].class, String.class, Map.class);
            method.setAccessible(true);
            return (CourseSlot[][]) method.invoke(service, userId, courseStructure, regionCode, categorySpotCache);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke private method", e);
        }
    }
}