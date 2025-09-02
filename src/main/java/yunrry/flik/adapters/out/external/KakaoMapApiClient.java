package yunrry.flik.adapters.out.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonProperty;

@Slf4j
@Component
public class KakaoMapApiClient {

    private final WebClient webClient;

    @Value("${kakao.api.key}")
    private String apiKey;

    public KakaoMapApiClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .defaultHeader("KA", "sdk/1.0 os/web origin/localhost")
                .build();
    }

    public Optional<KakaoLocationResponse.Document> searchLocation(String query) {
        try {
            KakaoLocationResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/keyword.json")
                            .queryParam("query", query)
                            .build())
                    .header("Authorization", "KakaoAK " + apiKey)
                    .retrieve()
                    .bodyToMono(KakaoLocationResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response != null &&
                    response.documents() != null &&
                    !response.documents().isEmpty()) {

                log.debug("검색 성공: {} -> 결과 {}개", query, response.documents().size());
                
                // 원본 JSON 응답 로깅 추가
                log.info("카카오 API 원본 응답:");
                for (int i = 0; i < response.documents().size(); i++) {
                    KakaoLocationResponse.Document doc = response.documents().get(i);
                    log.info("  결과 {}: x={}, y={}, placeName={}, addressName={}", 
                            i + 1, doc.x(), doc.y(), doc.placeName(), doc.addressName());
                }

                return Optional.of(response.documents().get(0));
            }

            log.warn("검색 결과 없음: {}", query);
            return Optional.empty();

        } catch (Exception e) {
            log.error("카카오 API 호출 실패: query={}, error={}", query, e.getMessage());
            return Optional.empty();
        }
    }

    public record KakaoLocationResponse(
            List<Document> documents
    ) {
        public record Document(
                @JsonProperty("x") String x, // longitude
                @JsonProperty("y") String y, // latitude
                @JsonProperty("place_name") String placeName,
                @JsonProperty("address_name") String addressName
        ) {
            public BigDecimal getLongitude() {
                return new BigDecimal(x);
            }

            public BigDecimal getLatitude() {
                return new BigDecimal(y);
            }

            public String getAddressName() {
                return addressName;
            }
        }
    }
}