package yunrry.flik.core.service.embedding;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import yunrry.flik.core.service.MetricsService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIEmbeddingService {

    private final WebClient openAIWebClient;
    private final MetricsService metricsService;

    @Value("${openai.embedding.model:text-embedding-3-small}")
    private String embeddingModel;

    public Mono<List<Double>> createEmbedding(String text) {
        Timer.Sample sample = metricsService.startEmbeddingTimer();
        Timer.Sample openAISample = metricsService.startOpenAITimer();

        if (text == null || text.trim().isEmpty()) {
            return Mono.just(Collections.nCopies(1536, 0.0));
        }

        log.debug("Creating embedding for text: {}", text.substring(0, Math.min(text.length(), 50)));

        Map<String, Object> request = Map.of(
                "model", embeddingModel,
                "input", text,
                "encoding_format", "float"
        );

        return openAIWebClient
                .post()
                .uri("/embeddings")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenAIEmbeddingResponse.class)
                .doOnNext(response -> {
                    // 응답 전체 로그 (개발/디버깅용)
                    log.debug("OpenAI Embedding Response: {}", response);
                    metricsService.incrementEmbedding("tag_embedding");
                    metricsService.recordOpenAIAPITime(openAISample, "embeddings");
                    // 응답 상태 로그
                    if (response.getData() != null && !response.getData().isEmpty()) {
                        log.info("Successfully created embedding - Model: {}, Dimension: {}",
                                embeddingModel, response.getData().get(0).getEmbedding().size());

                        // 임베딩 벡터의 처음 몇 개 값만 로그 (전체는 너무 길어서)
                        List<Double> embedding = response.getData().get(0).getEmbedding();
                        log.debug("Embedding preview (first 5 values): {}",
                                embedding.subList(0, Math.min(5, embedding.size())));
                    } else {
                        log.warn("Empty embedding response received");
                    }
                })
                .map(response -> {
                    if (response.getData() == null || response.getData().isEmpty()) {
                        return Collections.nCopies(1536, 0.0);
                    }
                    return response.getData().get(0).getEmbedding();
                })
                .onErrorReturn(Collections.nCopies(1536, 0.0))
                .doOnError(error -> {
                    log.error("Failed to create tag embedding: {}", error.getMessage());
                    metricsService.recordOpenAIAPITime(openAISample, "embeddings");
                })
                .doFinally(signalType -> {
                    // 임베딩 생성 시간 측정
                    metricsService.recordEmbeddingTime(sample, "tag_embedding");
                });
    }

    public Mono<List<String>> extractKeywords(String description, String reviews) {
        Timer.Sample sample = metricsService.startEmbeddingTimer();
        Timer.Sample openAISample = metricsService.startOpenAITimer();
        if ((description == null || description.trim().isEmpty()) &&
                (reviews == null || reviews.trim().isEmpty())) {
            return Mono.just(List.of());
        }

        String prompt = String.format("""
                다음 장소 정보에서 가장 중요한 키워드 3-5개를 추출해주세요.
                장소의 특성, 분위기, 특징을 잘 나타내는 명사나 형용사 위주로 선별해주세요.
                
                설명: %s
                리뷰: %s
                
                결과는 쉼표로 구분된 키워드만 반환해주세요.
                예: 카페, 조용한, 디저트, 데이트
                """, description != null ? description : "", reviews != null ? reviews : "");

        Map<String, Object> request = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 50,
                "temperature", 0.3
        );

        return openAIWebClient
                .post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenAIChatResponse.class)
                .doOnNext(response -> {
                    // 키워드 추출 성공 카운터
                    metricsService.incrementEmbedding("keyword_extraction");
                    // Chat 응답 로그
                    log.debug("OpenAI Chat Response: {}", response);
                    metricsService.recordOpenAIAPITime(openAISample, "chat_completions");
                    if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                        String content = response.getChoices().get(0).getMessage().getContent();
                        log.info("Extracted keywords response: {}", content);
                    } else {
                        log.warn("Empty chat response received");
                    }
                })
                .map(response -> {
                    if (response.getChoices() == null || response.getChoices().isEmpty()) {
                        return List.<String>of();
                    }
                    String content = response.getChoices().get(0).getMessage().getContent();
                    log.info("Raw OpenAI content: '{}'", content); // 추가

                    String[] keywords = content.split(",\\s*");
                    log.info("Split keywords: {}", Arrays.toString(keywords)); // 추가

                    return Arrays.asList(keywords);
                })
                .onErrorReturn(List.of())
                .doOnError(error -> {
                    log.error("Failed to extract keywords: {}", error.getMessage());
                    metricsService.recordOpenAIAPITime(openAISample, "chat_completions");
                })
                .doFinally(signalType -> {
                    // 키워드 추출 시간 측정
                    metricsService.recordEmbeddingTime(sample, "keyword_extraction");
                });
    }

    // Response DTOs
    public static class OpenAIEmbeddingResponse {
        private List<EmbeddingData> data;

        public List<EmbeddingData> getData() {
            return data;
        }

        public void setData(List<EmbeddingData> data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "OpenAIEmbeddingResponse{" +
                    "data=" + (data != null ? data.size() + " embeddings" : "null") +
                    '}';
        }
    }

    public static class EmbeddingData {
        private List<Double> embedding;

        public List<Double> getEmbedding() {
            return embedding;
        }

        public void setEmbedding(List<Double> embedding) {
            this.embedding = embedding;
        }

        @Override
        public String toString() {
            return "EmbeddingData{" +
                    "embedding=" + (embedding != null ? embedding.size() + " dimensions" : "null") +
                    '}';
        }
    }

    public static class OpenAIChatResponse {
        private List<Choice> choices;

        public List<Choice> getChoices() {
            return choices;
        }

        public void setChoices(List<Choice> choices) {
            this.choices = choices;
        }

        @Override
        public String toString() {
            return "OpenAIChatResponse{" +
                    "choices=" + (choices != null ? choices.size() + " choices" : "null") +
                    '}';
        }
    }

    public static class Choice {
        private Message message;

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "Choice{" +
                    "message=" + message +
                    '}';
        }
    }

    public static class Message {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "content='" + (content != null ? content.substring(0, Math.min(content.length(), 100)) + "..." : "null") + '\'' +
                    '}';
        }
    }
}