package yunrry.flik.core.service.TravelCourseRecommendation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import yunrry.flik.core.domain.exception.RecommendationException;
import yunrry.flik.core.domain.mapper.CategoryMapper;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.plan.CourseSlot;
import yunrry.flik.core.domain.model.plan.TravelCourse;
import yunrry.flik.core.service.plan.TravelCourseRecommendationService;
import yunrry.flik.core.service.plan.TravelPlannerService;
import yunrry.flik.core.service.plan.VectorSimilarityRecommendationService;
import yunrry.flik.core.service.spot.SpotCacheService;
import yunrry.flik.core.service.spot.SpotPreloadService;
import yunrry.flik.core.service.user.UserPreferenceService;
import yunrry.flik.ports.in.query.CourseQuery;
import yunrry.flik.ports.out.repository.SpotRepository;
import yunrry.flik.ports.out.repository.UserSavedSpotRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TravelCourseRecommendationServiceErrorTest {

    @InjectMocks
    private TravelCourseRecommendationService service;

    @Mock
    private SpotPreloadService spotPreloadService;

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
        regionCode = "11110";
        lenient().when(spotPreloadService.preloadAllCategorySpots(any(), anyLong(), anyString()))
                .thenReturn(createMockSpotCache());
    }

    @Test
    @DisplayName("TC-601: 후보 장소 없음 - 빈 슬롯 반환 + 경고 로그")
    void testNoCandidateSpots() {
        // Given
        CourseQuery query = CourseQuery.builder()
                .userId(userId)
                .selectedRegion(regionCode)
                .selectedCategories(Arrays.asList("nature"))
                .days(1)
                .build();

        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "nature", "restaurant", ""}
        };

        lenient().when(userPreferenceService.getUserMainCategoryCount(anyLong(), anyString()))
                .thenReturn(1);
        when(travelPlannerService.generateTravelCourse(any(), eq(1), anyMap()))
                .thenReturn(courseStructure);
        lenient().when(userSavedSpotRepository.findSpotIdsByUserId(userId))
                .thenReturn(Arrays.asList(1L, 2L, 3L));
        lenient().when(categoryMapper.getSubCategoryNames(any(MainCategory.class)))
                .thenReturn(Arrays.asList("sub1", "sub2"));
        lenient().when(spotRepository.findIdsByIdsAndLabelDepth2InAndRegnCd(anyList(), anyList(), eq(regionCode)))
                .thenReturn(Collections.emptyList());

        // When
        TravelCourse result = service.generatePersonalizedTravelCourse(query);

        // Then
        assertNotNull(result);
        CourseSlot[] day1 = result.getCourseSlots()[0];
        assertTrue(day1[1].getRecommendedSpotIds().isEmpty(), "자연 슬롯 비어있음");
        assertTrue(day1[3].getRecommendedSpotIds().isEmpty(), "자연 슬롯 비어있음");
    }

    @Test
    @DisplayName("TC-602: 유효하지 않은 카테고리 - 빈 슬롯 반환")
    void testInvalidCategory() {
        // Given
        CourseQuery query = CourseQuery.builder()
                .userId(userId)
                .selectedRegion(regionCode)
                .selectedCategories(Arrays.asList("nature"))
                .days(1)
                .build();

        String[][] courseStructure = {
                {"", "nature", "restaurant", "nature", "restaurant", ""}
        };

        setupCommonMocks();
        when(travelPlannerService.generateTravelCourse(any(), eq(1), anyMap()))
                .thenReturn(courseStructure);

        // When
        TravelCourse result = service.generatePersonalizedTravelCourse(query);

        // Then
        CourseSlot[] day1 = result.getCourseSlots()[0];
        assertTrue(day1[0].getRecommendedSpotIds().isEmpty(), "빈 타입은 빈 슬롯");
    }

    @Test
    @DisplayName("TC-603: DB 조회 실패 - 예외 전파")
    void testDatabaseQueryFailure() {
        // Given
        CourseQuery query = CourseQuery.builder()
                .userId(userId)
                .selectedRegion(regionCode)
                .selectedCategories(Arrays.asList("nature"))
                .days(1)
                .build();

        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "nature", "restaurant", ""}
        };

        lenient().when(userPreferenceService.getUserMainCategoryCount(anyLong(), anyString()))
                .thenReturn(1);
        lenient().when(travelPlannerService.generateTravelCourse(any(), eq(1), anyMap()))
                .thenReturn(courseStructure);

        // SpotPreloadService에서 예외 발생하도록 설정
        when(spotPreloadService.preloadAllCategorySpots(any(), anyLong(), anyString()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> service.generatePersonalizedTravelCourse(query));
    }

    @Test
    @DisplayName("TC-604: 벡터 유사도 계산 실패 - 예외 전파")
    void testVectorSimilarityFailure() {
        // Given
        CourseQuery query = CourseQuery.builder()
                .userId(userId)
                .selectedRegion(regionCode)
                .selectedCategories(Arrays.asList("nature"))
                .days(1)
                .build();

        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "nature", "restaurant", ""}
        };

        lenient().when(userPreferenceService.getUserMainCategoryCount(anyLong(), anyString()))
                .thenReturn(1);
        when(travelPlannerService.generateTravelCourse(any(), eq(1), anyMap()))
                .thenReturn(courseStructure);
        lenient().when(userSavedSpotRepository.findSpotIdsByUserId(userId))
                .thenReturn(Arrays.asList(1L, 2L, 3L));
        lenient().when(categoryMapper.getSubCategoryNames(any(MainCategory.class)))
                .thenReturn(Arrays.asList("sub1", "sub2"));
        lenient().when(spotRepository.findIdsByIdsAndLabelDepth2InAndRegnCd(anyList(), anyList(), anyString()))
                .thenReturn(Arrays.asList(301L, 302L, 303L));
        lenient().when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                anyLong(), anyList(), any(MainCategory.class), anyInt()))
                .thenThrow(new RecommendationException("Vector calculation failed",
                        new RuntimeException()));

        // When & Then
        assertThrows(RecommendationException.class,
                () -> service.generatePersonalizedTravelCourse(query));
    }



    @Test
    @DisplayName("TC-605: null userId - NullPointerException")
    void testNullUserId() {
        // Given
        CourseQuery query = CourseQuery.builder()
                .userId(null)
                .selectedRegion(regionCode)
                .selectedCategories(Arrays.asList("nature"))
                .days(1)
                .build();

        // When & Then
        assertThrows(Exception.class,
                () -> service.generatePersonalizedTravelCourse(query));
    }

    @Test
    @DisplayName("TC-606: 빈 카테고리 리스트 - 예외 또는 빈 코스")
    void testEmptyCategories() {
        // Given
        CourseQuery query = CourseQuery.builder()
                .userId(userId)
                .selectedRegion(regionCode)
                .selectedCategories(Collections.emptyList())
                .days(1)
                .build();

        // When & Then
        assertThrows(Exception.class,
                () -> service.generatePersonalizedTravelCourse(query));
    }

    @Test
    @DisplayName("TC-607: TravelPlannerService 실패 - 예외 전파")
    void testTravelPlannerFailure() {
        // Given
        CourseQuery query = CourseQuery.builder()
                .userId(userId)
                .selectedRegion(regionCode)
                .selectedCategories(Arrays.asList("nature"))
                .days(1)
                .build();

        when(userPreferenceService.getUserMainCategoryCount(anyLong(), anyString()))
                .thenReturn(1);
        when(travelPlannerService.generateTravelCourse(any(), anyInt(), anyMap()))
                .thenThrow(new RuntimeException("Failed to generate course structure"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> service.generatePersonalizedTravelCourse(query));
    }

    @Test
    @DisplayName("TC-608: UserPreferenceService 실패 - 예외 전파")
    void testUserPreferenceServiceFailure() {
        // Given
        CourseQuery query = CourseQuery.builder()
                .userId(userId)
                .selectedRegion(regionCode)
                .selectedCategories(Arrays.asList("nature"))
                .days(1)
                .build();

        when(userPreferenceService.getUserMainCategoryCount(anyLong(), anyString()))
                .thenThrow(new RuntimeException("Failed to get user preference"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> service.generatePersonalizedTravelCourse(query));
    }


    @Test
    @DisplayName("TC-610: 일부 슬롯만 실패 - 나머지는 정상 처리")
    void testPartialFailure() {
        // Given
        CourseQuery query = CourseQuery.builder()
                .userId(userId)
                .selectedRegion(regionCode)
                .selectedCategories(Arrays.asList("nature", "cafe"))
                .days(1)
                .build();

        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "nature", "restaurant", ""}
        };

        setupCommonMocks();
        when(travelPlannerService.generateTravelCourse(any(), eq(1), anyMap()))
                .thenReturn(courseStructure);

        // 자연은 성공, 카페는 실패
        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId), anyList(), eq(MainCategory.NATURE), anyInt()))
                .thenReturn(Arrays.asList(301L, 302L, 303L));
        when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                eq(userId), anyList(), eq(MainCategory.CAFE), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        TravelCourse result = service.generatePersonalizedTravelCourse(query);

        // Then
        CourseSlot[] day1 = result.getCourseSlots()[0];
        assertFalse(day1[1].getRecommendedSpotIds().isEmpty(), "자연 슬롯 성공");
        assertTrue(day1[0].getRecommendedSpotIds().isEmpty(), "카페 슬롯 빈칸");
    }

    // ========== Helper Methods ==========

    private void setupCommonMocks() {
        lenient().when(userPreferenceService.getUserMainCategoryCount(anyLong(), anyString()))
                .thenReturn(5);
        lenient().when(spotPreloadService.preloadAllCategorySpots(any(), anyLong(), anyString()))
                .thenReturn(createMockSpotCache());
        lenient().when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                        anyLong(), anyList(), any(MainCategory.class), anyInt()))
                .thenReturn(Arrays.asList(301L, 302L, 303L));
    }

    private Map<MainCategory, List<Long>> createMockSpotCache() {
        Map<MainCategory, List<Long>> cache = new HashMap<>();
        cache.put(MainCategory.CAFE, Arrays.asList(401L, 402L, 403L));
        cache.put(MainCategory.NATURE, Arrays.asList(301L, 302L, 303L));
        cache.put(MainCategory.RESTAURANT, Arrays.asList(101L, 102L, 103L, 104L, 105L));
        cache.put(MainCategory.ACCOMMODATION, Arrays.asList(201L, 202L, 203L));
        return cache;
    }
}