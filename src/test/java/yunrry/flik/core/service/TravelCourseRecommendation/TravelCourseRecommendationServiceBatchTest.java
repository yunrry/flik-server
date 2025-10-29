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
import yunrry.flik.core.service.spot.SpotCacheService;
import yunrry.flik.core.service.spot.SpotPreloadService;
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
    private SpotPreloadService spotPreloadService;

    @Mock
    private SpotCacheService spotCacheService;

    @Mock
    private UserSavedSpotRepository userSavedSpotRepository;

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

        // SpotCacheService Mock - 실제 로직 시뮬레이션
        lenient().when(spotCacheService.getCategorySpots(anyLong(), any(MainCategory.class), anyString()))
                .thenAnswer(invocation -> {
                    MainCategory category = invocation.getArgument(1, MainCategory.class);
                    List<Long> savedSpots = userSavedSpotRepository.findSpotIdsByUserId(userId);
                    List<String> subCategories = categoryMapper.getSubCategoryNames(category);
                    return spotRepository.findIdsByIdsAndLabelDepth2InAndRegnCd(
                            savedSpots, subCategories, regionCode);
                });

        lenient().when(userSavedSpotRepository.findSpotIdsByUserId(anyLong()))
                .thenReturn(Arrays.asList(1L, 2L, 3L));
        lenient().when(categoryMapper.getSubCategoryNames(any(MainCategory.class)))
                .thenReturn(Arrays.asList("sub1", "sub2"));
        lenient().when(spotRepository.findIdsByIdsAndLabelDepth2InAndRegnCd(anyList(), anyList(), anyString()))
                .thenReturn(Arrays.asList(301L, 302L, 303L));
    }

    @Test
    @DisplayName("TC-301: 필요한 카테고리 추출")
    void testExtractRequiredCategories() throws Exception {
        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "history_culture", "restaurant", "accommodation"}
        };

        Set<MainCategory> result = invokeExtractRequiredCategories(courseStructure);

        assertEquals(5, result.size());
        assertTrue(result.containsAll(Arrays.asList(
                MainCategory.CAFE, MainCategory.NATURE, MainCategory.HISTORY_CULTURE,
                MainCategory.RESTAURANT, MainCategory.ACCOMMODATION
        )));
    }

    @Test
    @DisplayName("TC-302: 카테고리별 일괄 조회 - DB 조회 횟수 확인")
    void testPreloadAllCategorySpotsQueryCount() throws Exception {
        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "history_culture", "restaurant", "accommodation"},
                {"cafe", "nature", "restaurant", "history_culture", "restaurant", "accommodation"}
        };

        setupMocks();
        Map<MainCategory, List<Long>> result = spotPreloadService.preloadAllCategorySpots(
                courseStructure, userId, regionCode);

        assertNotNull(result);
        assertEquals(5, result.size());
        verify(spotRepository, times(5)).findIdsByIdsAndLabelDepth2InAndRegnCd(
                anyList(), anyList(), eq(regionCode));
    }

    @Test
    @DisplayName("TC-303: 빈 코스 구조 - 기본 카테고리만")
    void testExtractRequiredCategoriesEmpty() throws Exception {
        String[][] courseStructure = {{"", "", "", "", "", ""}};

        Set<MainCategory> result = invokeExtractRequiredCategories(courseStructure);

        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList(
                MainCategory.RESTAURANT, MainCategory.ACCOMMODATION
        )));
    }

    @Test
    @DisplayName("TC-304: 중복 카테고리 제거")
    void testExtractRequiredCategoriesDeduplication() throws Exception {
        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "nature", "restaurant", "accommodation"},
                {"cafe", "nature", "restaurant", "nature", "restaurant", "accommodation"},
                {"cafe", "nature", "restaurant", "nature", "restaurant", ""}
        };

        Set<MainCategory> result = invokeExtractRequiredCategories(courseStructure);

        assertEquals(4, result.size());
    }

    @Test
    @DisplayName("TC-305: 일괄 조회 결과 검증")
    void testPreloadAllCategorySpotsResult() throws Exception {
        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "history_culture", "restaurant", "accommodation"}
        };

        setupMocks();
        Map<MainCategory, List<Long>> result = spotPreloadService.preloadAllCategorySpots(
                courseStructure, userId, regionCode);

        assertTrue(result.containsKey(MainCategory.CAFE));
        assertFalse(result.get(MainCategory.CAFE).isEmpty());
    }

    @Test
    @DisplayName("TC-306: 2박3일 코스 - DB 조회 최소화")
    void testPreloadThreeDaysMinimalQueries() throws Exception {
        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "history_culture", "restaurant", "accommodation"},
                {"cafe", "market", "restaurant", "nature", "restaurant", "accommodation"},
                {"cafe", "history_culture", "restaurant", "market", "restaurant", ""}
        };

        setupMocks();
        Map<MainCategory, List<Long>> result = spotPreloadService.preloadAllCategorySpots(
                courseStructure, userId, regionCode);

        assertEquals(6, result.size());
        verify(spotRepository, times(6)).findIdsByIdsAndLabelDepth2InAndRegnCd(
                anyList(), anyList(), eq(regionCode));
    }

    @Test
    @DisplayName("TC-310: 빈 장소 목록 처리")
    void testPreloadEmptySpotList() throws Exception {
        String[][] courseStructure = {
                {"cafe", "nature", "restaurant", "history_culture", "restaurant", "accommodation"}
        };

        when(userSavedSpotRepository.findSpotIdsByUserId(userId))
                .thenReturn(Collections.emptyList());
        when(categoryMapper.getSubCategoryNames(any(MainCategory.class)))
                .thenReturn(Arrays.asList("sub1", "sub2"));
        when(spotRepository.findIdsByIdsAndLabelDepth2InAndRegnCd(anyList(), anyList(), eq(regionCode)))
                .thenReturn(Collections.emptyList());

        Map<MainCategory, List<Long>> result = spotPreloadService.preloadAllCategorySpots(
                courseStructure, userId, regionCode);

        assertEquals(5, result.size());
        assertTrue(result.values().stream().allMatch(List::isEmpty));
    }

    // ========== Helper Methods ==========

    private void setupMocks() {
        when(userSavedSpotRepository.findSpotIdsByUserId(userId))
                .thenReturn(Arrays.asList(1L, 2L, 3L, 4L, 5L));
        when(categoryMapper.getSubCategoryNames(any(MainCategory.class)))
                .thenReturn(Arrays.asList("sub1", "sub2"));
        when(spotRepository.findIdsByIdsAndLabelDepth2InAndRegnCd(anyList(), anyList(), eq(regionCode)))
                .thenReturn(Arrays.asList(101L, 102L, 103L, 104L, 105L));
    }

    private Set<MainCategory> invokeExtractRequiredCategories(String[][] courseStructure) throws Exception {
        Method method = SpotPreloadService.class
                .getDeclaredMethod("extractRequiredCategories", String[][].class);
        method.setAccessible(true);
        return (Set<MainCategory>) method.invoke(spotPreloadService, (Object) courseStructure);
    }
}