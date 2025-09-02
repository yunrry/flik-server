package yunrry.flik.adatpters.out.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class NaverLocalSearchApiClient {

    private final WebClient webClient;

    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;

    public NaverLocalSearchApiClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://openapi.naver.com")
                .build();
    }

    public Optional<NaverLocalResponse.Item> searchLocation(String query) {
        try {
            NaverLocalResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/search/local.json")
                            .queryParam("query", query)
                            .queryParam("display", 5)
                            .queryParam("start", 1)
                            .queryParam("sort", "random")
                            .build())
                    .header("X-Naver-Client-Id", clientId)
                    .header("X-Naver-Client-Secret", clientSecret)
                    .retrieve()
                    .bodyToMono(NaverLocalResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response != null &&
                    response.items() != null &&
                    !response.items().isEmpty()) {

                log.debug("네이버 지역검색 성공: {} -> 결과 {}개", query, response.items().size());
                
                // 모든 검색 결과 로깅
                log.info("네이버 지역검색 결과 상세:");
                for (int i = 0; i < response.items().size(); i++) {
                    NaverLocalResponse.Item item = response.items().get(i);
                    log.info("  결과 {}: 제목={}, 주소={}, 도로명주소={}, 좌표=({}, {})", 
                            i + 1, item.title(), item.address(), item.roadAddress(), item.mapx(), item.mapy());
                }

                return Optional.of(response.items().get(0));
            }

            log.warn("네이버 지역검색 결과 없음: {}", query);
            return Optional.empty();

        } catch (Exception e) {
            log.error("네이버 지역검색 API 호출 실패: query={}, error={}", query, e.getMessage());
            return Optional.empty();
        }
    }

    public record NaverLocalResponse(
            List<Item> items
    ) {
        public record Item(
                String title,
                String link,
                String category,
                String description,
                String telephone,
                String address,
                String roadAddress,
                String mapx,
                String mapy
        ) {
            public BigDecimal getLongitude() {
                return new BigDecimal(mapx);
            }

            public BigDecimal getLatitude() {
                return new BigDecimal(mapy);
            }
        }
    }
}
