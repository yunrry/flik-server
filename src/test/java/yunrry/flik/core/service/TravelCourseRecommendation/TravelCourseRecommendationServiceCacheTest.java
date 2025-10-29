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
import yunrry.flik.ports.out.repository.SpotRepository;
import yunrry.flik.ports.out.repository.UserSavedSpotRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TravelCourseRecommendationServiceCacheTest {

    @InjectMocks
    private TravelCourseRecommendationService service;

    @Mock
    private UserSavedSpotRepository userSavedSpotRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private SpotRepository spotRepository;

    @BeforeEach
    void setUp() {
        when(userSavedSpotRepository.findSpotIdsByUserId(anyLong()))
                .thenReturn(Arrays.asList(1L, 2L, 3L));
        when(categoryMapper.getSubCategoryNames(any(MainCategory.class)))
                .thenReturn(Arrays.asList("sub1", "sub2"));
        when(spotRepository.findIdsByIdsAndLabelDepth2InAndRegnCd(any(), any(), any()))
                .thenReturn(Arrays.asList(301L, 302L, 303L));
    }

    @Test
    @DisplayName("TC-401: 캐시 없이 DB 조회 발생")
    void testWithoutCache() {
        // When
        List<Long> result = service.getCategorySpotsWithCache(1L, MainCategory.NATURE, "11");

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(spotRepository).findIdsByIdsAndLabelDepth2InAndRegnCd(any(), any(), eq("11"));
    }

    @Test
    @DisplayName("TC-402: 여러 번 호출 시 매번 DB 조회 (캐시 없음)")
    void testMultipleCalls() {
        // When
        service.getCategorySpotsWithCache(1L, MainCategory.NATURE, "11");
        service.getCategorySpotsWithCache(1L, MainCategory.NATURE, "11");

        // Then - 단위 테스트에서는 캐시 동작 안 함
        verify(spotRepository, times(2)).findIdsByIdsAndLabelDepth2InAndRegnCd(any(), any(), eq("11"));
    }
}