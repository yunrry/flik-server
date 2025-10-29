package yunrry.flik.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import yunrry.flik.core.domain.exception.RecommendationException;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.embedding.SpotSimilarity;
import yunrry.flik.core.service.embedding.SpotEmbeddingService;
import yunrry.flik.core.service.plan.VectorSimilarityRecommendationService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VectorSimilarityRecommendationServiceTest {

    @InjectMocks
    private VectorSimilarityRecommendationService service;

    @Mock
    private SpotEmbeddingService spotEmbeddingService;

    private Long userId;
    private MainCategory category;
    private List<Long> candidateSpotIds;

    @BeforeEach
    void setUp() {
        userId = 1L;
        category = MainCategory.NATURE;
        candidateSpotIds = Arrays.asList(301L, 302L, 303L, 304L, 305L);
    }

    // ========== 1. 정상 추천 테스트 ==========

    @Test
    @DisplayName("TC-001: SpotSimilarity 반환")
    void testFindRecommendedSpots() {
        // Given
        int limit = 3;
        List<SpotSimilarity> expected = Arrays.asList(
                new SpotSimilarity(301L, 0.95),
                new SpotSimilarity(302L, 0.90),
                new SpotSimilarity(303L, 0.85)
        );

        when(spotEmbeddingService.findSimilarSpotsByUserPreference(
                eq(userId), eq(category.getCode()), eq(candidateSpotIds), eq(limit)))
                .thenReturn(expected);

        // When
        List<SpotSimilarity> result = service.findRecommendedSpotsByVectorSimilarity(
                userId, candidateSpotIds, category, limit);

        // Then
        assertEquals(3, result.size());
        assertEquals(expected, result);
        verify(spotEmbeddingService).findSimilarSpotsByUserPreference(
                userId, category.getCode(), candidateSpotIds, limit);
    }

    @Test
    @DisplayName("TC-002: ID만 반환")
    void testFindRecommendedSpotIds() {
        // Given
        int limit = 3;
        List<SpotSimilarity> spotSimilarities = Arrays.asList(
                new SpotSimilarity(301L, 0.95),
                new SpotSimilarity(302L, 0.90),
                new SpotSimilarity(303L, 0.85)
        );

        when(spotEmbeddingService.findSimilarSpotsByUserPreference(
                eq(userId), eq(category.getCode()), eq(candidateSpotIds), eq(limit)))
                .thenReturn(spotSimilarities);

        // When
        List<Long> result = service.findRecommendedSpotIdsByVectorSimilarity(
                userId, candidateSpotIds, category, limit);

        // Then
        assertEquals(3, result.size());
        assertEquals(Arrays.asList(301L, 302L, 303L), result);
    }

    @Test
    @DisplayName("TC-003: limit보다 적은 후보")
    void testLessThanLimit() {
        // Given
        List<Long> fewCandidates = Arrays.asList(301L, 302L);
        int limit = 5;
        List<SpotSimilarity> expected = Arrays.asList(
                new SpotSimilarity(301L, 0.95),
                new SpotSimilarity(302L, 0.90)
        );

        when(spotEmbeddingService.findSimilarSpotsByUserPreference(
                eq(userId), eq(category.getCode()), eq(fewCandidates), eq(limit)))
                .thenReturn(expected);

        // When
        List<SpotSimilarity> result = service.findRecommendedSpotsByVectorSimilarity(
                userId, fewCandidates, category, limit);

        // Then
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("TC-004: 정확히 limit 개수")
    void testExactLimit() {
        // Given
        List<Long> exactCandidates = Arrays.asList(301L, 302L, 303L);
        int limit = 3;
        List<SpotSimilarity> expected = Arrays.asList(
                new SpotSimilarity(301L, 0.95),
                new SpotSimilarity(302L, 0.90),
                new SpotSimilarity(303L, 0.85)
        );

        when(spotEmbeddingService.findSimilarSpotsByUserPreference(
                eq(userId), eq(category.getCode()), eq(exactCandidates), eq(limit)))
                .thenReturn(expected);

        // When
        List<SpotSimilarity> result = service.findRecommendedSpotsByVectorSimilarity(
                userId, exactCandidates, category, limit);

        // Then
        assertEquals(3, result.size());
    }

    // ========== 2. 입력 검증 테스트 ==========

    @Test
    @DisplayName("TC-201: null 후보 - 빈 리스트 반환")
    void testNullCandidates() {
        // When
        List<SpotSimilarity> result = service.findRecommendedSpotsByVectorSimilarity(
                userId, null, category, 3);

        // Then
        assertTrue(result.isEmpty());
        verify(spotEmbeddingService, never()).findSimilarSpotsByUserPreference(
                anyLong(), anyString(), anyList(), anyInt());
    }

    @Test
    @DisplayName("TC-202: 빈 후보 - 빈 리스트 반환")
    void testEmptyCandidates() {
        // When
        List<SpotSimilarity> result = service.findRecommendedSpotsByVectorSimilarity(
                userId, Collections.emptyList(), category, 3);

        // Then
        assertTrue(result.isEmpty());
        verify(spotEmbeddingService, never()).findSimilarSpotsByUserPreference(
                anyLong(), anyString(), anyList(), anyInt());
    }

    @Test
    @DisplayName("TC-203: limit 0 - 빈 리스트 반환")
    void testZeroLimit() {
        // When
        List<SpotSimilarity> result = service.findRecommendedSpotsByVectorSimilarity(
                userId, candidateSpotIds, category, 0);

        // Then
        assertTrue(result.isEmpty());
        verify(spotEmbeddingService, never()).findSimilarSpotsByUserPreference(
                anyLong(), anyString(), anyList(), anyInt());
    }

    @Test
    @DisplayName("TC-204: limit 음수 - 빈 리스트 반환")
    void testNegativeLimit() {
        // When
        List<SpotSimilarity> result = service.findRecommendedSpotsByVectorSimilarity(
                userId, candidateSpotIds, category, -1);

        // Then
        assertTrue(result.isEmpty());
        verify(spotEmbeddingService, never()).findSimilarSpotsByUserPreference(
                anyLong(), anyString(), anyList(), anyInt());
    }

    @Test
    @DisplayName("TC-205: 정상 입력 - 추천 반환")
    void testValidInput() {
        // Given
        List<SpotSimilarity> expected = Arrays.asList(
                new SpotSimilarity(301L, 0.95)
        );

        when(spotEmbeddingService.findSimilarSpotsByUserPreference(
                anyLong(), anyString(), anyList(), anyInt()))
                .thenReturn(expected);

        // When
        List<SpotSimilarity> result = service.findRecommendedSpotsByVectorSimilarity(
                userId, candidateSpotIds, category, 3);

        // Then
        assertFalse(result.isEmpty());
    }

    // ========== 3. 예외 처리 테스트 ==========

    @Test
    @DisplayName("TC-301: SpotEmbedding 조회 실패 - RecommendationException")
    void testSpotEmbeddingFailure() {
        // Given
        when(spotEmbeddingService.findSimilarSpotsByUserPreference(
                anyLong(), anyString(), anyList(), anyInt()))
                .thenThrow(new RuntimeException("DB connection failed"));

        // When & Then
        assertThrows(RecommendationException.class,
                () -> service.findRecommendedSpotsByVectorSimilarity(
                        userId, candidateSpotIds, category, 3));
    }

    @Test
    @DisplayName("TC-302: 예외 메시지에 userId, category 포함")
    void testExceptionMessage() {
        // Given
        when(spotEmbeddingService.findSimilarSpotsByUserPreference(
                anyLong(), anyString(), anyList(), anyInt()))
                .thenThrow(new RuntimeException("Error"));

        // When & Then
        RecommendationException ex = assertThrows(RecommendationException.class,
                () -> service.findRecommendedSpotsByVectorSimilarity(
                        userId, candidateSpotIds, category, 3));

        assertTrue(ex.getMessage().contains(userId.toString()));
        assertTrue(ex.getMessage().contains(category.toString()));
    }

    @Test
    @DisplayName("TC-303: 예외 원인 검증")
    void testExceptionCause() {
        // Given
        RuntimeException cause = new RuntimeException("Root cause");
        when(spotEmbeddingService.findSimilarSpotsByUserPreference(
                anyLong(), anyString(), anyList(), anyInt()))
                .thenThrow(cause);

        // When & Then
        RecommendationException ex = assertThrows(RecommendationException.class,
                () -> service.findRecommendedSpotsByVectorSimilarity(
                        userId, candidateSpotIds, category, 3));

        assertNotNull(ex.getCause());
        assertEquals(cause, ex.getCause());
    }

    // ========== 4. 트랜잭션 테스트 ==========

    @Test
    @DisplayName("TC-401: @Transactional 어노테이션 확인")
    void testTransactionalAnnotation() throws NoSuchMethodException {
        // Given
        var method = VectorSimilarityRecommendationService.class
                .getMethod("findRecommendedSpotsByVectorSimilarity",
                        Long.class, List.class, MainCategory.class, int.class);

        // Then
        assertTrue(method.isAnnotationPresent(
                org.springframework.transaction.annotation.Transactional.class));
    }

    // ========== 6. 통합 테스트 ==========

    @Test
    @DisplayName("TC-601: 전체 플로우")
    void testFullFlow() {
        // Given
        List<SpotSimilarity> expected = Arrays.asList(
                new SpotSimilarity(301L, 0.95),
                new SpotSimilarity(302L, 0.90)
        );

        when(spotEmbeddingService.findSimilarSpotsByUserPreference(
                eq(userId), eq(category.getCode()), eq(candidateSpotIds), eq(3)))
                .thenReturn(expected);

        // When
        List<SpotSimilarity> result = service.findRecommendedSpotsByVectorSimilarity(
                userId, candidateSpotIds, category, 3);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(spotEmbeddingService).findSimilarSpotsByUserPreference(
                userId, category.getCode(), candidateSpotIds, 3);
    }

    @Test
    @DisplayName("TC-602: 다양한 카테고리")
    void testVariousCategories() {
        // Given
        MainCategory[] categories = {
                MainCategory.NATURE, MainCategory.CAFE,
                MainCategory.HISTORY_CULTURE, MainCategory.MARKET
        };

        when(spotEmbeddingService.findSimilarSpotsByUserPreference(
                anyLong(), anyString(), anyList(), anyInt()))
                .thenReturn(Arrays.asList(new SpotSimilarity(301L, 0.95)));

        // When & Then
        for (MainCategory cat : categories) {
            List<SpotSimilarity> result = service.findRecommendedSpotsByVectorSimilarity(
                    userId, candidateSpotIds, cat, 3);
            assertFalse(result.isEmpty());
        }
    }

    @Test
    @DisplayName("TC-603: 대량 데이터")
    void testLargeDataset() {
        // Given
        List<Long> largeCandidates = new ArrayList<>();
        for (long i = 1; i <= 100; i++) {
            largeCandidates.add(i);
        }

        List<SpotSimilarity> expected = new ArrayList<>();
        for (long i = 1; i <= 10; i++) {
            expected.add(new SpotSimilarity(i, 0.9));
        }

        when(spotEmbeddingService.findSimilarSpotsByUserPreference(
                eq(userId), eq(category.getCode()), eq(largeCandidates), eq(10)))
                .thenReturn(expected);

        // When
        List<SpotSimilarity> result = service.findRecommendedSpotsByVectorSimilarity(
                userId, largeCandidates, category, 10);

        // Then
        assertEquals(10, result.size());
    }
}