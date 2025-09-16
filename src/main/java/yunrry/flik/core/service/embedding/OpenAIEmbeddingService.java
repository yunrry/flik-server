package yunrry.flik.core.service.embedding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIEmbeddingService {

    private final WebClient openAIWebClient;

    @Value("${openai.embedding.model:text-embedding-3-small}")
    private String embeddingModel;

    public Mono<List<Double>> createEmbedding(String text) {
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
                .map(response -> {
                    if (response.getData() == null || response.getData().isEmpty()) {
                        return Collections.nCopies(1536, 0.0);
                    }
                    return response.getData().get(0).getEmbedding();
                })
                .onErrorReturn(Collections.nCopies(1536, 0.0))
                .doOnError(error -> log.error("Failed to create embedding: {}", error.getMessage()));
    }

    public Mono<List<String>> extractKeywords(String description, String reviews) {
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
                "model", "gpt-3.5-turbo",
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
                .map(response -> {
                    if (response.getChoices() == null || response.getChoices().isEmpty()) {
                        return List.<String>of();
                    }
                    String content = response.getChoices().get(0).getMessage().getContent();
                    return Arrays.asList(content.split(",\\s*"));
                })
                .onErrorReturn(List.of())
                .doOnError(error -> log.error("Failed to extract keywords: {}", error.getMessage()));
    }

    // Response DTOs
    public static class OpenAIEmbeddingResponse {
        private List<EmbeddingData> data;

        public List<EmbeddingData> getData() { return data; }
        public void setData(List<EmbeddingData> data) { this.data = data; }
    }

    public static class EmbeddingData {
        private List<Double> embedding;

        public List<Double> getEmbedding() { return embedding; }
        public void setEmbedding(List<Double> embedding) { this.embedding = embedding; }
    }

    public static class OpenAIChatResponse {
        private List<Choice> choices;

        public List<Choice> getChoices() { return choices; }
        public void setChoices(List<Choice> choices) { this.choices = choices; }
    }

    public static class Choice {
        private Message message;

        public Message getMessage() { return message; }
        public void setMessage(Message message) { this.message = message; }
    }

    public static class Message {
        private String content;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}

