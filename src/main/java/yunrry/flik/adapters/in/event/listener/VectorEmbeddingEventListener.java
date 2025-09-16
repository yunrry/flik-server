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
        return updateSpotService.findById(spotId)
                .flatMap(spot -> {
                    // 1. 태그가 없다면 AI로 추출
                    if (needsTagExtraction(spot)) {
                        return extractAndSaveTags(spot)
                                .then(updateSpotService.findById(spotId));
                    }
                    return Mono.just(spot);
                })
                .flatMap(spot -> {
                    // 2. 위치 임베딩 생성
                    Mono<String> locationEmbedding = vectorProcessingService
                            .createLocationEmbedding(spot.getLatitude(), spot.getLongitude());

                    // 3. 태그 임베딩 생성
                    Mono<String> tagEmbedding = vectorProcessingService
                            .createTagEmbedding(spot.getTag1(), spot.getTag2(), spot.getTag3(),
                                    spot.getTags(), spot.getLabelDepth1(), spot.getLabelDepth2(), spot.getLabelDepth3());

                    // 4. PostgreSQL에 저장
                    return Mono.zip(locationEmbedding, tagEmbedding)
                            .flatMap(tuple -> {
                                SpotEmbedding embedding = SpotEmbedding.builder()
                                        .spotId(spotId)
                                        .locationEmbedding(parseVector(tuple.getT1()))
                                        .tagEmbedding(parseVector(tuple.getT2()))
                                        .build();
                                return spotEmbeddingService.saveOrUpdateEmbedding(embedding);
                            })
                            .then();
                });
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