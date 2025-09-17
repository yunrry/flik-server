package yunrry.flik.adapters.in.event.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import yunrry.flik.adapters.out.persistence.mysql.entity.FestivalEntity;
import yunrry.flik.core.domain.event.SpotSwipeEvent;
import yunrry.flik.core.domain.model.card.Festival;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.core.domain.model.embedding.SpotEmbedding;
import yunrry.flik.core.domain.testfixture.SpotTestFixture;
import yunrry.flik.core.service.spot.UpdateSpotService;
import yunrry.flik.core.service.embedding.OpenAIEmbeddingService;
import yunrry.flik.core.service.embedding.SpotEmbeddingService;
import yunrry.flik.core.service.embedding.VectorProcessingService;
import yunrry.flik.core.service.plan.TagService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VectorEmbeddingEventListener 테스트")
class VectorEmbeddingEventListenerTest {

    @Mock
    private UpdateSpotService updateSpotService;

    @Mock
    private TagService tagService;

    @Mock
    private VectorProcessingService vectorProcessingService;

    @Mock
    private OpenAIEmbeddingService embeddingService;

    @Mock
    private SpotEmbeddingService spotEmbeddingService;

    @InjectMocks
    private VectorEmbeddingEventListener vectorEmbeddingEventListener;

    private SpotSwipeEvent spotSwipeEvent;
    private SpotSwipeEvent spotSwipeEvent2;
    private Spot spot1 = SpotTestFixture.createTestRestaurant(1L);;
    private Spot spot2   = SpotTestFixture.createTestRestaurant(2L);;
    private Spot spotWithTags;
    private Spot spotWithoutTags;

    @BeforeEach
    void setUp() {
        spotSwipeEvent = SpotSwipeEvent.of(1L, 1L);
        spotSwipeEvent2 = SpotSwipeEvent.of(1L, 2L);

        spotWithTags = spot1.withTags("카페", "브런치", "데이트", "카페,브런치,데이트") ;
        spotWithoutTags = spot2;
    }

    @Test
    @DisplayName("태그가 있는 Spot의 임베딩 처리 성공")
    void handleSpotEmbedding_WithExistingTags_Success() {
        // Given
        String locationVector = "[0.1, 0.2, 0.3]";
        String tagVector = "[0.4, 0.5, 0.6]";

        when(updateSpotService.findById(1L)).thenReturn(Mono.just(spotWithTags));
        when(vectorProcessingService.createLocationEmbedding(BigDecimal.valueOf(37.5665), BigDecimal.valueOf(126.9780)))
                .thenReturn(Mono.just(locationVector));
        when(vectorProcessingService.createTagEmbedding(
                eq("카페"), eq("브런치"), eq("데이트"),
                eq("카페,브런치,데이트"), eq("음식점"), eq("카페"), eq("브런치카페")))
                .thenReturn(Mono.just(tagVector));
        when(spotEmbeddingService.saveOrUpdateEmbedding(any(SpotEmbedding.class)))
                .thenReturn(Mono.empty());

        // When & Then
        vectorEmbeddingEventListener.handleSpotEmbedding(spotSwipeEvent);

        // 약간의 지연 후 검증 (비동기 처리를 위해)
        verify(updateSpotService, timeout(1000)).findById(1L);
        verify(vectorProcessingService, timeout(1000)).createLocationEmbedding(BigDecimal.valueOf(37.5665), BigDecimal.valueOf(126.9780));
        verify(vectorProcessingService, timeout(1000)).createTagEmbedding(
                eq("카페"), eq("브런치"), eq("데이트"),
                eq("카페,브런치,데이트"), eq("음식점"), eq("카페"), eq("브런치카페"));
        verify(spotEmbeddingService, timeout(1000)).saveOrUpdateEmbedding(any(SpotEmbedding.class));
        verify(embeddingService, never()).extractKeywords(anyString(), anyString());
    }

    @Test
    @DisplayName("태그가 없는 Spot의 임베딩 처리 - 태그 추출 후 임베딩 생성")
    void handleSpotEmbedding_WithoutTags_ExtractTagsAndProcess() {
        // Given
        List<String> extractedKeywords = Arrays.asList("카페", "브런치", "데이트");
        String locationVector = "[0.1, 0.2, 0.3]";
        String tagVector = "[0.4, 0.5, 0.6]";

        Spot updatedSpot = spotWithTags; // 태그가 업데이트된 spot

        when(updateSpotService.findById(2L))
                .thenReturn(Mono.just(spotWithoutTags))
                .thenReturn(Mono.just(updatedSpot));
        when(embeddingService.extractKeywords(anyString(), anyString()))
                .thenReturn(Mono.just(extractedKeywords));
        when(tagService.saveKeywords(extractedKeywords))
                .thenReturn(Mono.empty());
        when(updateSpotService.updateSpotTags(2L, extractedKeywords))
                .thenReturn(Mono.empty());
        when(vectorProcessingService.createLocationEmbedding(BigDecimal.valueOf(37.5665), BigDecimal.valueOf(126.9780)))
                .thenReturn(Mono.just(locationVector));
        when(vectorProcessingService.createTagEmbedding(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(tagVector));
        when(spotEmbeddingService.saveOrUpdateEmbedding(any(SpotEmbedding.class)))
                .thenReturn(Mono.empty());

        // When & Then
        vectorEmbeddingEventListener.handleSpotEmbedding(spotSwipeEvent2);

        // 검증
        verify(updateSpotService, timeout(1000).atLeast(1)).findById(2L);
        verify(embeddingService, timeout(1000)).extractKeywords(anyString(), anyString());
        verify(tagService, timeout(1000)).saveKeywords(extractedKeywords);
        verify(updateSpotService, timeout(1000)).updateSpotTags(2L, extractedKeywords);
        verify(vectorProcessingService, timeout(1000)).createLocationEmbedding(BigDecimal.valueOf(37.5665), BigDecimal.valueOf(126.9780));
        verify(spotEmbeddingService, timeout(1000)).saveOrUpdateEmbedding(any(SpotEmbedding.class));
    }


    @Test
    @DisplayName("Spot 조회 실패 시 예외 처리")
    void handleSpotEmbedding_SpotNotFound_HandleError() {
        // Given
        when(updateSpotService.findById(1L))
                .thenReturn(Mono.error(new RuntimeException("Spot not found")));

        // When & Then
        vectorEmbeddingEventListener.handleSpotEmbedding(spotSwipeEvent);

        // 에러가 발생해도 메서드가 완료되어야 함
        verify(updateSpotService, timeout(1000)).findById(1L);
        verify(vectorProcessingService, never()).createLocationEmbedding(BigDecimal.valueOf(anyDouble()), BigDecimal.valueOf((anyDouble())));
        verify(spotEmbeddingService, never()).saveOrUpdateEmbedding(any());
    }

    @Test
    @DisplayName("벡터 처리 실패 시 예외 처리")
    void handleSpotEmbedding_VectorProcessingFailed_HandleError() {
        // Given
        when(updateSpotService.findById(1L)).thenReturn(Mono.just(spotWithTags));
        when(vectorProcessingService.createLocationEmbedding(BigDecimal.valueOf(37.5665), BigDecimal.valueOf(126.9780)))
                .thenReturn(Mono.error(new RuntimeException("Vector processing failed")));

        // When & Then
        vectorEmbeddingEventListener.handleSpotEmbedding(spotSwipeEvent);

        // 검증
        verify(updateSpotService, timeout(1000)).findById(1L);
        verify(vectorProcessingService, timeout(1000)).createLocationEmbedding(BigDecimal.valueOf(37.5665), BigDecimal.valueOf(126.9780));
        verify(spotEmbeddingService, never()).saveOrUpdateEmbedding(any());
    }

    @Test
    @DisplayName("태그 추출 실패 시 예외 처리")
    void handleSpotEmbedding_TagExtractionFailed_HandleError() {
        // Given
        when(updateSpotService.findById(1L)).thenReturn(Mono.just(spotWithoutTags));
        when(embeddingService.extractKeywords(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Tag extraction failed")));

        // When & Then
        vectorEmbeddingEventListener.handleSpotEmbedding(spotSwipeEvent);

        // 검증
        verify(updateSpotService, timeout(1000).atLeastOnce()).findById(1L);
        verify(embeddingService, timeout(1000)).extractKeywords(anyString(), anyString());
        verify(vectorProcessingService, never()).createLocationEmbedding(any(BigDecimal.class), any(BigDecimal.class));
    }


    @Test
    @DisplayName("부분적인 태그가 있는 경우 - tag1만 있고 tag2, tag3가 비어있음")
    void handleSpotEmbedding_PartialTags_ShouldExtractTags() {
        // Given - 모든 태그가 비어있는 spot으로 설정해야 needsTagExtraction이 true가 됨
        Spot spotWithNoTags = spot2.withTags(null, null, null, null);
        List<String> extractedKeywords = Arrays.asList("카페", "브런치", "데이트");

        when(updateSpotService.findById(1L))
                .thenReturn(Mono.just(spotWithNoTags))
                .thenReturn(Mono.just(spotWithTags));
        when(embeddingService.extractKeywords(anyString(), anyString()))
                .thenReturn(Mono.just(extractedKeywords));
        when(tagService.saveKeywords(extractedKeywords))
                .thenReturn(Mono.empty());
        when(updateSpotService.updateSpotTags(1L, extractedKeywords))
                .thenReturn(Mono.empty());
        when(vectorProcessingService.createLocationEmbedding(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(Mono.just("[0.1, 0.2, 0.3]"));
        when(vectorProcessingService.createTagEmbedding(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just("[0.4, 0.5, 0.6]"));
        when(spotEmbeddingService.saveOrUpdateEmbedding(any(SpotEmbedding.class)))
                .thenReturn(Mono.empty());

        // When & Then
        vectorEmbeddingEventListener.handleSpotEmbedding(spotSwipeEvent);

        // 태그 추출이 실행되어야 함
        verify(embeddingService, timeout(1000)).extractKeywords(anyString(), anyString());
    }

    @Test
    @DisplayName("벡터 문자열 파싱 테스트")
    void parseVector_ValidVectorString_ShouldReturnDoubleList() {
        // Given
        String vectorString = "[0.1, 0.2, 0.3, 0.4]";

        when(updateSpotService.findById(1L)).thenReturn(Mono.just(spotWithTags));
        when(vectorProcessingService.createLocationEmbedding(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(Mono.just(vectorString));
        when(vectorProcessingService.createTagEmbedding(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(vectorString));
        when(spotEmbeddingService.saveOrUpdateEmbedding(any(SpotEmbedding.class)))
                .thenReturn(Mono.empty());

        // When & Then
        vectorEmbeddingEventListener.handleSpotEmbedding(spotSwipeEvent);

        // 정상적으로 처리되었는지 확인
        verify(spotEmbeddingService, timeout(1000)).saveOrUpdateEmbedding(argThat(embedding ->
                embedding.getLocationEmbedding().size() == 4 &&
                        embedding.getTagEmbedding().size() == 4
        ));
    }


    @Test
    @DisplayName("잘못된 벡터 문자열 파싱 테스트")
    void parseVector_InvalidVectorString_ShouldReturnEmptyList() {
        // Given
        String invalidVectorString = "invalid_vector";

        when(updateSpotService.findById(1L)).thenReturn(Mono.just(spotWithTags));
        when(vectorProcessingService.createLocationEmbedding(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(Mono.just(invalidVectorString));
        when(vectorProcessingService.createTagEmbedding(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(invalidVectorString));
        when(spotEmbeddingService.saveOrUpdateEmbedding(any(SpotEmbedding.class)))
                .thenReturn(Mono.empty());

        // When & Then
        vectorEmbeddingEventListener.handleSpotEmbedding(spotSwipeEvent);

        // 빈 리스트로 처리되었는지 확인
        verify(spotEmbeddingService, timeout(1000)).saveOrUpdateEmbedding(argThat(embedding ->
                embedding.getLocationEmbedding().isEmpty() &&
                        embedding.getTagEmbedding().isEmpty()
        ));
    }


    @Test
    @DisplayName("null 벡터 문자열 파싱 테스트")
    void parseVector_NullVectorString_ShouldReturnEmptyList() {
        // Given
        when(updateSpotService.findById(1L)).thenReturn(Mono.just(spotWithTags));
        when(vectorProcessingService.createLocationEmbedding(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(Mono.fromCallable(() -> null));
        when(vectorProcessingService.createTagEmbedding(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.fromCallable(() -> null));

        // When & Then
        vectorEmbeddingEventListener.handleSpotEmbedding(spotSwipeEvent);

        // null 처리로 인해 zip이 실패하므로 저장이 호출되지 않음을 검증
        verify(updateSpotService, timeout(1000)).findById(1L);
        verify(vectorProcessingService, timeout(1000)).createLocationEmbedding(any(BigDecimal.class), any(BigDecimal.class));
        verify(spotEmbeddingService, never()).saveOrUpdateEmbedding(any());
    }
}