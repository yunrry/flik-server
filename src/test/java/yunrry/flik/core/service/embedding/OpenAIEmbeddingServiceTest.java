package yunrry.flik.core.service.embedding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import yunrry.flik.core.service.MetricsService;
import yunrry.flik.core.service.embedding.OpenAIEmbeddingService.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OpenAIEmbeddingService 테스트")
class OpenAIEmbeddingServiceTest {

    @Mock
    private WebClient openAIWebClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private OpenAIEmbeddingService openAIEmbeddingService;

    private List<Double> sampleEmbedding;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(openAIEmbeddingService, "embeddingModel", "text-embedding-3-small");
        sampleEmbedding = Arrays.asList(0.1, 0.2, 0.3);

        when(openAIWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("임베딩 생성 성공")
    void createEmbedding_Success() {
        // Given
        String text = "카페에서 브런치를 먹었다";

        OpenAIEmbeddingResponse response = new OpenAIEmbeddingResponse();
        EmbeddingData embeddingData = new EmbeddingData();
        embeddingData.setEmbedding(sampleEmbedding);
        response.setData(List.of(embeddingData));

        when(responseSpec.bodyToMono(OpenAIEmbeddingResponse.class))
                .thenReturn(Mono.just(response));

        // When & Then
        StepVerifier.create(openAIEmbeddingService.createEmbedding(text))
                .expectNext(sampleEmbedding)
                .verifyComplete();

        verify(openAIWebClient).post();
        verify(requestBodyUriSpec).uri("/embeddings");
        verify(requestBodySpec).bodyValue(any());
    }

    @Test
    @DisplayName("임베딩 생성 - null 입력")
    void createEmbedding_NullInput_ReturnsDefaultEmbedding() {
        // When & Then
        StepVerifier.create(openAIEmbeddingService.createEmbedding(null))
                .expectNext(Collections.nCopies(1536, 0.0))
                .verifyComplete();

        verify(openAIWebClient, never()).post();
    }

    @Test
    @DisplayName("임베딩 생성 - 빈 문자열 입력")
    void createEmbedding_EmptyInput_ReturnsDefaultEmbedding() {
        // When & Then
        StepVerifier.create(openAIEmbeddingService.createEmbedding("  "))
                .expectNext(Collections.nCopies(1536, 0.0))
                .verifyComplete();

        verify(openAIWebClient, never()).post();
    }

    @Test
    @DisplayName("임베딩 생성 - API 에러 시 기본값 반환")
    void createEmbedding_ApiError_ReturnsDefaultEmbedding() {
        // Given
        when(responseSpec.bodyToMono(OpenAIEmbeddingResponse.class))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        // When & Then
        StepVerifier.create(openAIEmbeddingService.createEmbedding("test"))
                .expectNext(Collections.nCopies(1536, 0.0))
                .verifyComplete();
    }

    @Test
    @DisplayName("임베딩 생성 - 빈 응답 시 기본값 반환")
    void createEmbedding_EmptyResponse_ReturnsDefaultEmbedding() {
        // Given
        OpenAIEmbeddingResponse response = new OpenAIEmbeddingResponse();
        response.setData(List.of());

        when(responseSpec.bodyToMono(OpenAIEmbeddingResponse.class))
                .thenReturn(Mono.just(response));

        // When & Then
        StepVerifier.create(openAIEmbeddingService.createEmbedding("test"))
                .expectNext(Collections.nCopies(1536, 0.0))
                .verifyComplete();
    }

    @Test
    @DisplayName("키워드 추출 성공")
    void extractKeywords_Success() {
        // Given
        String description = "아늑한 카페";
        String reviews = "커피가 맛있고 분위기가 좋아요";

        OpenAIChatResponse response = new OpenAIChatResponse();
        Choice choice = new Choice();
        Message message = new Message();
        message.setContent("카페, 아늑한, 커피, 분위기");
        choice.setMessage(message);
        response.setChoices(List.of(choice));

        when(responseSpec.bodyToMono(OpenAIChatResponse.class))
                .thenReturn(Mono.just(response));

        // When & Then
        StepVerifier.create(openAIEmbeddingService.extractKeywords(description, reviews))
                .expectNext(Arrays.asList("카페", "아늑한", "커피", "분위기"))
                .verifyComplete();

        verify(requestBodyUriSpec).uri("/chat/completions");
    }

    @Test
    @DisplayName("키워드 추출 - null 입력")
    void extractKeywords_NullInput_ReturnsEmptyList() {
        // When & Then
        StepVerifier.create(openAIEmbeddingService.extractKeywords(null, null))
                .expectNext(List.of())
                .verifyComplete();

        verify(openAIWebClient, never()).post();
    }

    @Test
    @DisplayName("키워드 추출 - 빈 입력")
    void extractKeywords_EmptyInput_ReturnsEmptyList() {
        // When & Then
        StepVerifier.create(openAIEmbeddingService.extractKeywords("", "  "))
                .expectNext(List.of())
                .verifyComplete();

        verify(openAIWebClient, never()).post();
    }

    @Test
    @DisplayName("키워드 추출 - API 에러 시 빈 리스트 반환")
    void extractKeywords_ApiError_ReturnsEmptyList() {
        // Given
        when(responseSpec.bodyToMono(OpenAIChatResponse.class))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        // When & Then
        StepVerifier.create(openAIEmbeddingService.extractKeywords("test", "review"))
                .expectNext(List.of())
                .verifyComplete();
    }

    @Test
    @DisplayName("키워드 추출 - 빈 응답 시 빈 리스트 반환")
    void extractKeywords_EmptyResponse_ReturnsEmptyList() {
        // Given
        OpenAIChatResponse response = new OpenAIChatResponse();
        response.setChoices(List.of());

        when(responseSpec.bodyToMono(OpenAIChatResponse.class))
                .thenReturn(Mono.just(response));

        // When & Then
        StepVerifier.create(openAIEmbeddingService.extractKeywords("test", "review"))
                .expectNext(List.of())
                .verifyComplete();
    }

    @Test
    @DisplayName("키워드 추출 - 설명만 있는 경우")
    void extractKeywords_DescriptionOnly_Success() {
        // Given
        String description = "조용한 도서관 카페";

        OpenAIChatResponse response = new OpenAIChatResponse();
        Choice choice = new Choice();
        Message message = new Message();
        message.setContent("도서관, 조용한, 카페");
        choice.setMessage(message);
        response.setChoices(List.of(choice));

        when(responseSpec.bodyToMono(OpenAIChatResponse.class))
                .thenReturn(Mono.just(response));

        // When & Then
        StepVerifier.create(openAIEmbeddingService.extractKeywords(description, null))
                .expectNext(Arrays.asList("도서관", "조용한", "카페"))
                .verifyComplete();
    }

    @Test
    @DisplayName("키워드 추출 - 리뷰만 있는 경우")
    void extractKeywords_ReviewsOnly_Success() {
        // Given
        String reviews = "디저트가 정말 맛있어요";

        OpenAIChatResponse response = new OpenAIChatResponse();
        Choice choice = new Choice();
        Message message = new Message();
        message.setContent("디저트, 맛있는");
        choice.setMessage(message);
        response.setChoices(List.of(choice));

        when(responseSpec.bodyToMono(OpenAIChatResponse.class))
                .thenReturn(Mono.just(response));

        // When & Then
        StepVerifier.create(openAIEmbeddingService.extractKeywords(null, reviews))
                .expectNext(Arrays.asList("디저트", "맛있는"))
                .verifyComplete();
    }
}