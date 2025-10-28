package yunrry.flik.core.service.TravelCourseRecommendation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import yunrry.flik.core.domain.mapper.CategoryMapper;
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
class TravelCourseRecommendationServicePreferenceTest {

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

    @BeforeEach
    void setUp() {
        userId = 1L;
    }

    @Test
    @DisplayName("TC-101: 빈도수 정상 조회")
    void testGetUserSelectFrequencyNormal() throws Exception {
        // Given
        String[] selectedCategories = {"nature", "cafe"};

        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("자연")))
                .thenReturn(10);
        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("카페")))
                .thenReturn(5);

        // When
        Map<String, Integer> result = invokeGetUserSelectFrequency(selectedCategories);

        // Then
        assertNotNull(result, "빈도 맵이 null이 아니어야 함");
        assertEquals(2, result.size(), "2개 카테고리");
        assertEquals(10, result.get("nature"), "자연 빈도 10");
        assertEquals(5, result.get("cafe"), "카페 빈도 5");

        verify(userPreferenceService).getUserMainCategoryCount(userId, "자연");
        verify(userPreferenceService).getUserMainCategoryCount(userId, "카페");
    }

    @Test
    @DisplayName("TC-102: 빈도수 0인 경우 기본값 1 설정")
    void testGetUserSelectFrequencyZero() throws Exception {
        // Given
        String[] selectedCategories = {"nature", "cafe"};

        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("자연")))
                .thenReturn(0);
        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("카페")))
                .thenReturn(0);

        // When
        Map<String, Integer> result = invokeGetUserSelectFrequency(selectedCategories);

        // Then
        assertEquals(1, result.get("nature"), "빈도 0일 때 기본값 1");
        assertEquals(1, result.get("cafe"), "빈도 0일 때 기본값 1");
    }

    @Test
    @DisplayName("TC-103: 저장 기록 없는 사용자 - 모든 카테고리 빈도 1")
    void testGetUserSelectFrequencyNoHistory() throws Exception {
        // Given
        String[] selectedCategories = {"nature", "cafe", "history_culture"};

        // 모든 카테고리 빈도 0 반환
        when(userPreferenceService.getUserMainCategoryCount(anyLong(), anyString()))
                .thenReturn(0);

        // When
        Map<String, Integer> result = invokeGetUserSelectFrequency(selectedCategories);

        // Then
        assertEquals(3, result.size(), "3개 카테고리");
        assertEquals(1, result.get("nature"), "기본값 1");
        assertEquals(1, result.get("cafe"), "기본값 1");
        assertEquals(1, result.get("history_culture"), "기본값 1");

        verify(userPreferenceService, times(3))
                .getUserMainCategoryCount(eq(userId), anyString());
    }

    @Test
    @DisplayName("TC-104: 다양한 빈도수")
    void testGetUserSelectFrequencyVaried() throws Exception {
        // Given
        String[] selectedCategories = {"nature", "cafe", "history_culture", "market"};

        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("자연")))
                .thenReturn(20);
        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("카페")))
                .thenReturn(15);
        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("역사문화")))
                .thenReturn(8);
        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("시장")))
                .thenReturn(5);

        // When
        Map<String, Integer> result = invokeGetUserSelectFrequency(selectedCategories);

        // Then
        assertEquals(4, result.size(), "4개 카테고리");
        assertEquals(20, result.get("nature"), "자연 20");
        assertEquals(15, result.get("cafe"), "카페 15");
        assertEquals(8, result.get("history_culture"), "문화 8");
        assertEquals(5, result.get("market"), "시장 5");
    }

    @Test
    @DisplayName("TC-105: 일부 카테고리만 빈도 있음")
    void testGetUserSelectFrequencyPartial() throws Exception {
        // Given
        String[] selectedCategories = {"nature", "cafe", "history_culture"};

        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("자연")))
                .thenReturn(10);
        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("카페")))
                .thenReturn(0);
        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("역사문화")))
                .thenReturn(5);

        // When
        Map<String, Integer> result = invokeGetUserSelectFrequency(selectedCategories);

        // Then
        assertEquals(10, result.get("nature"), "자연 10");
        assertEquals(1, result.get("cafe"), "카페 기본값 1");
        assertEquals(5, result.get("history_culture"), "문화 5");
    }

    @Test
    @DisplayName("TC-106: 매우 높은 빈도수")
    void testGetUserSelectFrequencyHighCount() throws Exception {
        // Given
        String[] selectedCategories = {"nature"};

        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("자연")))
                .thenReturn(100);

        // When
        Map<String, Integer> result = invokeGetUserSelectFrequency(selectedCategories);

        // Then
        assertEquals(100, result.get("nature"), "높은 빈도수 처리");
    }

    @Test
    @DisplayName("TC-107: 빈 카테고리 배열")
    void testGetUserSelectFrequencyEmpty() throws Exception {
        // Given
        String[] selectedCategories = {};

        // When
        Map<String, Integer> result = invokeGetUserSelectFrequency(selectedCategories);

        // Then
        assertTrue(result.isEmpty(), "빈 맵 반환");
        verify(userPreferenceService, never())
                .getUserMainCategoryCount(anyLong(), anyString());
    }

    @Test
    @DisplayName("TC-108: 모든 MainCategory 타입 처리")
    void testGetUserSelectFrequencyAllCategories() throws Exception {
        // Given
        String[] selectedCategories = {
                "nature", "indoor", "history_culture", "cafe",
                "activity", "festival", "market", "themepark"
        };

        // 각 카테고리별 빈도 설정
        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("자연")))
                .thenReturn(10);
        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("실내")))
                .thenReturn(8);
        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("역사문화")))
                .thenReturn(7);
        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("카페")))
                .thenReturn(6);
        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("액티비티")))
                .thenReturn(5);
        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("축제")))
                .thenReturn(4);
        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("시장")))
                .thenReturn(3);
        when(userPreferenceService.getUserMainCategoryCount(eq(userId), eq("테마파크")))
                .thenReturn(2);

        // When
        Map<String, Integer> result = invokeGetUserSelectFrequency(selectedCategories);

        // Then
        assertEquals(8, result.size(), "8개 카테고리");
        assertEquals(10, result.get("nature"));
        assertEquals(8, result.get("indoor"));
        assertEquals(7, result.get("history_culture"));
        assertEquals(6, result.get("cafe"));
        assertEquals(5, result.get("activity"));
        assertEquals(4, result.get("festival"));
        assertEquals(3, result.get("market"));
        assertEquals(2, result.get("themepark"));
    }

    // ========== Helper Methods ==========

    /**
     * private 메서드 getUserSelectFrequency 호출
     */
    private Map<String, Integer> invokeGetUserSelectFrequency(String[] selectedCategories) throws Exception {
        Method method = TravelCourseRecommendationService.class
                .getDeclaredMethod("getUserSelectFrequency", Long.class, String[].class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Integer> result = (Map<String, Integer>) method.invoke(service, userId, selectedCategories);

        return result;
    }
}