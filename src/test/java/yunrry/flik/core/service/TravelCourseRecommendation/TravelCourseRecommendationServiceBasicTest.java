package yunrry.flik.core.service.TravelCourseRecommendation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.annotation.Profile;
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
class TravelCourseRecommendationServiceBasicTest {

    @InjectMocks
    private TravelCourseRecommendationService service;

    @Mock
    private TravelPlannerService travelPlannerService;

    @Mock
    private SpotPreloadService spotPreloadService;

    @Mock
    private SpotCacheService spotCacheService;

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

        lenient().when(spotPreloadService.preloadAllCategorySpots(any(), anyLong(), anyString()))
                .thenReturn(createMockSpotCache());
    }


    @Test
    @DisplayName("TC-001: 당일치기 코스 생성")
    void testOneDayTrip() {
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

        // Mock 설정
        setupCommonMocks();
        when(travelPlannerService.generateTravelCourse(any(), eq(1), anyMap()))
                .thenReturn(courseStructure);

        setupLenientRecommendationMocks();

        // When
        TravelCourse result = service.generatePersonalizedTravelCourse(query);

        // Then
        assertNotNull(result, "코스가 생성되어야 함");
        assertEquals(1, result.getDays(), "1일");
        assertEquals(1, result.getCourseSlots().length, "1일치 슬롯");
        assertEquals(6, result.getCourseSlots()[0].length, "하루 6개 슬롯");

        // 슬롯 타입 검증
        CourseSlot[] day1 = result.getCourseSlots()[0];
        assertEquals(MainCategory.CAFE, day1[0].getMainCategory(), "슬롯0: 카페");
        assertEquals(MainCategory.NATURE, day1[1].getMainCategory(), "슬롯1: 자연");
        assertEquals(MainCategory.RESTAURANT, day1[2].getMainCategory(), "슬롯2: 점심");
        assertEquals(MainCategory.NATURE, day1[3].getMainCategory(), "슬롯3: 자연");
        assertEquals(MainCategory.RESTAURANT, day1[4].getMainCategory(), "슬롯4: 저녁");
        assertTrue(day1[5].getRecommendedSpotIds().isEmpty(), "슬롯5: 빈칸");

        verify(travelPlannerService).generateTravelCourse(any(), eq(1), anyMap());
    }

    @Test
    @DisplayName("TC-002: 1박2일 코스 생성")
    void testTwoDaysTrip() {
        // Given
        CourseQuery query = CourseQuery.builder()
                .userId(userId)
                .selectedRegion(regionCode)
                .selectedCategories(Arrays.asList("nature", "history_culture"))
                .days(2)
                .build();

        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "history_culture", "restaurant", "accommodation"},
                {"cafe", "nature", "restaurant", "history_culture", "restaurant", ""}
        };

        // Mock 설정
        setupCommonMocks();
        when(travelPlannerService.generateTravelCourse(any(), eq(2), anyMap()))
                .thenReturn(courseStructure);

        setupLenientRecommendationMocks();

        // When
        TravelCourse result = service.generatePersonalizedTravelCourse(query);

        // Then
        assertNotNull(result, "코스가 생성되어야 함");
        assertEquals(2, result.getDays(), "2일");
        assertEquals(2, result.getCourseSlots().length, "2일치 슬롯");

        // Day 1 검증
        CourseSlot[] day1 = result.getCourseSlots()[0];
        assertEquals(MainCategory.CAFE, day1[0].getMainCategory(), "DAY1 슬롯0: 카페");
        assertEquals(MainCategory.NATURE, day1[1].getMainCategory(), "DAY1 슬롯1: 자연");
        assertEquals(MainCategory.RESTAURANT, day1[2].getMainCategory(), "DAY1 슬롯2: 점심");
        assertEquals(MainCategory.HISTORY_CULTURE, day1[3].getMainCategory(), "DAY1 슬롯3: 문화");
        assertEquals(MainCategory.RESTAURANT, day1[4].getMainCategory(), "DAY1 슬롯4: 저녁");
        assertEquals(MainCategory.ACCOMMODATION, day1[5].getMainCategory(), "DAY1 슬롯5: 숙박");

        // Day 2 검증
        CourseSlot[] day2 = result.getCourseSlots()[1];
        assertEquals(MainCategory.CAFE, day2[0].getMainCategory(), "DAY2 슬롯0: 카페");
        assertTrue(day2[5].getRecommendedSpotIds().isEmpty(), "DAY2 슬롯5: 빈칸");

        // 숙박 검증
        assertEquals(1, countAccommodationSlots(result), "숙박 1개");

        verify(travelPlannerService).generateTravelCourse(any(), eq(2), anyMap());
    }

    @Test
    @DisplayName("TC-003: 2박3일 코스 생성")
    void testThreeDaysTrip() {
        // Given
        CourseQuery query = CourseQuery.builder()
                .userId(userId)
                .selectedRegion(regionCode)
                .selectedCategories(Arrays.asList("nature", "cafe", "history_culture"))
                .days(3)
                .build();

        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "history_culture", "restaurant", "accommodation"},
                {"cafe", "nature", "restaurant", "history_culture", "restaurant", "accommodation"},
                {"cafe", "nature", "restaurant", "history_culture", "restaurant", ""}
        };

        // Mock 설정
        setupCommonMocks();
        when(travelPlannerService.generateTravelCourse(any(), eq(3), anyMap()))
                .thenReturn(courseStructure);

        setupLenientRecommendationMocks();

        // When
        TravelCourse result = service.generatePersonalizedTravelCourse(query);

        // Then
        assertNotNull(result, "코스가 생성되어야 함");
        assertEquals(3, result.getDays(), "3일");
        assertEquals(3, result.getCourseSlots().length, "3일치 슬롯");

        // 전체 슬롯 개수
        int totalSlots = Arrays.stream(result.getCourseSlots())
                .mapToInt(day -> day.length)
                .sum();
        assertEquals(18, totalSlots, "총 18개 슬롯");

        // 각 날짜 검증
        for (int day = 0; day < 3; day++) {
            CourseSlot[] daySlots = result.getCourseSlots()[day];
            assertEquals(6, daySlots.length, String.format("DAY%d 6개 슬롯", day + 1));
            assertEquals(MainCategory.CAFE, daySlots[0].getMainCategory(),
                    String.format("DAY%d 첫 슬롯 카페", day + 1));
        }

        // 숙박 검증
        assertEquals(2, countAccommodationSlots(result), "숙박 2개");
        assertTrue(result.getCourseSlots()[2][5].getRecommendedSpotIds().isEmpty(), "DAY3 마지막 빈칸");

        verify(travelPlannerService).generateTravelCourse(any(), eq(3), anyMap());
    }

    @Test
    @DisplayName("TC-004: 모든 슬롯에 추천 장소 있음")
    void testAllSlotsHaveRecommendations() {
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

        // Mock 설정
        setupCommonMocks();
        when(travelPlannerService.generateTravelCourse(any(), eq(1), anyMap()))
                .thenReturn(courseStructure);

        setupLenientRecommendationMocks();

        // When
        TravelCourse result = service.generatePersonalizedTravelCourse(query);

        // Then
        CourseSlot[] day1 = result.getCourseSlots()[0];

        // 빈 슬롯 제외하고 모두 추천 있어야 함
        for (int i = 0; i < 5; i++) {
            assertFalse(day1[i].getRecommendedSpotIds().isEmpty(),
                    String.format("슬롯%d는 추천이 있어야 함", i));
            assertTrue(day1[i].getRecommendedSpotIds().size() > 0,
                    String.format("슬롯%d는 최소 1개 이상의 추천", i));
        }

        // 마지막 슬롯만 비어있어야 함
        assertTrue(day1[5].getRecommendedSpotIds().isEmpty(), "슬롯5는 비어있어야 함");

        // 각 슬롯별 추천 개수 검증
        assertTrue(day1[0].getRecommendedSpotIds().size() >= 1, "카페 추천 1개 이상");
        assertTrue(day1[1].getRecommendedSpotIds().size() >= 1, "자연 추천 1개 이상");
        assertTrue(day1[2].getRecommendedSpotIds().size() >= 1, "점심 추천 1개 이상");
        assertTrue(day1[3].getRecommendedSpotIds().size() >= 1, "자연 추천 1개 이상");
        assertTrue(day1[4].getRecommendedSpotIds().size() >= 1, "저녁 추천 1개 이상");
    }

    // ========== Helper Methods ==========



    private void setupCommonMocks() {
        lenient().when(userPreferenceService.getUserMainCategoryCount(anyLong(), anyString()))
                .thenReturn(5);
        lenient().when(userSavedSpotRepository.findSpotIdsByUserId(userId))
                .thenReturn(Arrays.asList(1L, 2L, 3L, 4L, 5L));
        lenient().when(categoryMapper.getSubCategoryNames(any(MainCategory.class)))
                .thenReturn(Arrays.asList("sub1", "sub2"));
        lenient().when(spotRepository.findIdsByIdsAndLabelDepth2InAndRegnCd(anyList(), anyList(), eq(regionCode)))
                .thenReturn(Arrays.asList(301L, 302L, 303L, 304L, 305L));
    }

    private void setupLenientRecommendationMocks() {
        // anyInt()를 사용하여 모든 limit 값 허용
        lenient().when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                        eq(userId), anyList(), eq(MainCategory.CAFE), anyInt()))
                .thenReturn(Arrays.asList(401L, 402L, 403L));

        lenient().when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                        eq(userId), anyList(), eq(MainCategory.NATURE), anyInt()))
                .thenReturn(Arrays.asList(301L, 302L, 303L));

        lenient().when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                        eq(userId), anyList(), eq(MainCategory.HISTORY_CULTURE), anyInt()))
                .thenReturn(Arrays.asList(501L, 502L, 503L));

        lenient().when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                        eq(userId), anyList(), eq(MainCategory.RESTAURANT), anyInt()))
                .thenReturn(Arrays.asList(101L, 102L, 103L, 104L, 105L, 106L, 107L, 108L, 109L, 110L));

        lenient().when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                        eq(userId), anyList(), eq(MainCategory.ACCOMMODATION), anyInt()))
                .thenReturn(Arrays.asList(201L, 202L, 203L, 204L, 205L));

        // 다른 카테고리들
        lenient().when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                        eq(userId), anyList(), eq(MainCategory.INDOOR), anyInt()))
                .thenReturn(Arrays.asList(601L, 602L, 603L));

        lenient().when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                        eq(userId), anyList(), eq(MainCategory.ACTIVITY), anyInt()))
                .thenReturn(Arrays.asList(701L, 702L, 703L));

        lenient().when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                        eq(userId), anyList(), eq(MainCategory.FESTIVAL), anyInt()))
                .thenReturn(Arrays.asList(801L, 802L, 803L));

        lenient().when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                        eq(userId), anyList(), eq(MainCategory.MARKET), anyInt()))
                .thenReturn(Arrays.asList(901L, 902L, 903L));

        lenient().when(vectorSimilarityRecommendationService.findRecommendedSpotIdsByVectorSimilarity(
                        eq(userId), anyList(), eq(MainCategory.THEMEPARK), anyInt()))
                .thenReturn(Arrays.asList(1001L, 1002L, 1003L));
    }

    private int countAccommodationSlots(TravelCourse course) {
        int count = 0;
        for (CourseSlot[] day : course.getCourseSlots()) {
            for (CourseSlot slot : day) {
                if (slot.getMainCategory() == MainCategory.ACCOMMODATION
                        && !slot.getRecommendedSpotIds().isEmpty()) {
                    count++;
                }
            }
        }
        return count;
    }

    private Map<MainCategory, List<Long>> createMockSpotCache() {
        Map<MainCategory, List<Long>> cache = new HashMap<>();
        cache.put(MainCategory.CAFE, Arrays.asList(401L, 402L, 403L));
        cache.put(MainCategory.NATURE, Arrays.asList(301L, 302L, 303L));
        cache.put(MainCategory.RESTAURANT, Arrays.asList(101L, 102L, 103L, 104L, 105L));
        cache.put(MainCategory.ACCOMMODATION, Arrays.asList(201L, 202L, 203L));
        cache.put(MainCategory.HISTORY_CULTURE, Arrays.asList(501L, 502L, 503L));
        return cache;
    }
}