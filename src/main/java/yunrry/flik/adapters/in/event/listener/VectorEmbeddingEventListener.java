package yunrry.flik.adapters.in.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import reactor.core.publisher.Mono;

import yunrry.flik.core.domain.event.SpotSwipeEvent;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.core.domain.model.embedding.SpotEmbedding;
import yunrry.flik.core.service.spot.UpdateSpotService;
import yunrry.flik.core.service.embedding.OpenAIEmbeddingService;
import yunrry.flik.core.service.embedding.SpotEmbeddingService;
import yunrry.flik.core.service.embedding.VectorProcessingService;
import yunrry.flik.core.service.plan.TagService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class VectorEmbeddingEventListener {

    private final UpdateSpotService updateSpotService;
    private final TagService tagService;
    private final VectorProcessingService vectorProcessingService;
    private final OpenAIEmbeddingService embeddingService;
    private final SpotEmbeddingService spotEmbeddingService;

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSpotEmbedding(SpotSwipeEvent event) {
        try {
            log.info("Processing vector embedding for spot: {}", event.getSpotId());

            processSpotEmbedding(event.getSpotId())
                    .doOnSuccess(result -> log.info("Successfully processed embedding for spot: {}", event.getSpotId()))
                    .doOnError(error -> log.error("Failed to process embedding for spot: {} - Error: {}",
                            event.getSpotId(), error.getMessage()))
                    .subscribe();

        } catch (Exception e) {
            log.error("Failed to process vector embedding for spot: {} - Error: {}",
                    event.getSpotId(), e.getMessage(), e);
        }
    }

    private Mono<Void> processSpotEmbedding(Long spotId) {
        return spotEmbeddingService.findBySpotId(spotId)
                .hasElement() // SpotEmbedding 존재 여부만 확인
                .flatMap(exists -> {
                    if (exists) {
                        log.info("SpotEmbedding already exists for spotId: {}", spotId);
                        return Mono.empty(); // 이미 존재하면 아무 작업도 하지 않음
                    }

                    // 존재하지 않을 경우 생성 로직 수행
                    return updateSpotService.findById(spotId)
                            .flatMap(spot -> {
                                // 1. 태그가 없으면 AI로 태그 추출 후 최신 Spot 정보 다시 조회
                                if (needsTagExtraction(spot)) {
                                    log.info("Extracting tags for spotId: {}", spotId);
                                    return extractAndSaveTags(spot)
                                            .then(updateSpotService.findById(spotId));
                                }
                                log.info("Spot already has tags for spotId: {}", spotId);
                                return Mono.just(spot);
                            })
                            .flatMap(spot -> {
                                // 2. 위치 임베딩 생성
                                Mono<String> locationEmbeddingMono =
                                        vectorProcessingService.createLocationEmbedding(
                                                spot.getLatitude(),
                                                spot.getLongitude()
                                        );

                                // 3. 태그 임베딩 생성
                                Mono<String> tagEmbeddingMono =
                                        vectorProcessingService.createTagEmbedding(
                                                spot.getTag1(),
                                                spot.getTag2(),
                                                spot.getTag3(),
                                                spot.getTags(),
                                                spot.getLabelDepth1(),
                                                spot.getLabelDepth2(),
                                                spot.getLabelDepth3()
                                        );

                                // 4. PostgreSQL에 임베딩 저장
                                return Mono.zip(locationEmbeddingMono, tagEmbeddingMono)
                                        .flatMap(tuple -> {
                                            SpotEmbedding newEmbedding = SpotEmbedding.builder()
                                                    .spotId(spotId)
                                                    .locationEmbedding(parseVector(tuple.getT1()))
                                                    .tagEmbedding(parseVector(tuple.getT2()))
                                                    .build();

                                            return spotEmbeddingService.saveOrUpdateEmbedding(newEmbedding)
                                                    .doOnSuccess(v -> log.info("Created SpotEmbedding for spotId: {}", spotId));
                                        });
                            });
                })
                .then();
    }

    private boolean needsTagExtraction(Spot spot) {
        return (spot.getTag1() == null || spot.getTag1().trim().isEmpty()) &&
                (spot.getTag2() == null || spot.getTag2().trim().isEmpty()) &&
                (spot.getTag3() == null || spot.getTag3().trim().isEmpty());
    }

    private Mono<Void> extractAndSaveTags(Spot spot) {
        return embeddingService.extractKeywords(spot.getDescription(), spot.getGoogleReviews())
                .flatMap(keywords -> {
                    return tagService.saveKeywords(keywords)
                            .then(updateSpotService.updateSpotTags(spot.getId(), keywords));
                });
    }

    private List<Double> parseVector(String vectorString) {
        if (vectorString == null || vectorString.trim().isEmpty()) {
            return List.of();
        }
        try {
            String cleaned = vectorString.replace("[", "").replace("]", "");
            return Arrays.stream(cleaned.split(","))
                    .map(String::trim)
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to parse vector: {}", vectorString);
            return List.of();
        }
    }
}