package yunrry.flik.core.service.embedding;

import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import yunrry.flik.core.domain.model.embedding.SpotSimilarity;
import yunrry.flik.core.domain.model.embedding.SpotEmbedding;
import yunrry.flik.core.service.MetricsService;
import yunrry.flik.ports.out.repository.SpotEmbeddingRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotEmbeddingService {

    private final SpotEmbeddingRepository spotEmbeddingRepository;
    private final MetricsService metricsService;

    public List<SpotSimilarity> findSimilarSpotsByUserPreference(Long userId,
                                                          String category,
                                                          List<Long> spotIds,
                                                          int limit) {
        return spotEmbeddingRepository.findSimilarSpotsByUserPreference(userId, category, spotIds, limit);
    }




    public Mono<Optional<SpotEmbedding>> getAsyncEmbeddingById(Long spotId) {
        return Mono.fromCallable(() -> {
                    log.debug("Fetching embedding for spot: {}", spotId);
                    return spotEmbeddingRepository.findBySpotId(spotId);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(error -> log.error("Failed to fetch embedding for spot {}: {}", spotId, error.getMessage()));
    }

    public Mono<Optional<String>> getAsyncTagEmbedding(Long spotId) {
        return getAsyncEmbeddingById(spotId)
                .map(optionalEmbedding -> optionalEmbedding
                        .map(SpotEmbedding::getTagEmbeddingAsString)
                        .filter(tag -> tag != null && !tag.isEmpty()));
    }


    public Mono<SpotEmbedding> saveOrUpdateEmbedding(SpotEmbedding spotEmbedding) {
        Timer.Sample sample = metricsService.startEmbeddingTimer();

        return Mono.fromCallable(() -> spotEmbeddingRepository.save(spotEmbedding))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(saved -> {
                    // 임베딩 저장 성공 카운터
                    metricsService.incrementEmbedding("spot_embedding_save");

                    // 벡터 크기 메트릭 (태그 임베딩이 있는 경우)
                    if (saved.getTagEmbedding() != null && !saved.getTagEmbedding().isEmpty()) {
                        double[] vector = saved.getTagEmbedding().stream()
                                .mapToDouble(Double::doubleValue)
                                .toArray();
                        metricsService.recordSpotFeatureVector("save", "tag", vector);
                    }

                    log.info("Saved embedding for spot: {}", saved.getSpotId());
                })
                .doFinally(signalType -> {
                    // 임베딩 저장 시간 측정
                    metricsService.recordEmbeddingTime(sample, "embedding_save");
                });
    }

    public Mono<List<SpotEmbedding>> getAsyncEmbeddingsBySpotIds(List<Long> spotIds) {
        return Mono.fromCallable(() -> spotEmbeddingRepository.findBySpotIds(spotIds))
                .subscribeOn(Schedulers.boundedElastic());
    }


    public Optional<SpotEmbedding> getEmbeddingById(Long spotId) {
        log.debug("Fetching embedding for spot: {}", spotId);
        return spotEmbeddingRepository.findBySpotId(spotId);
    }

    public Optional<String> getTagEmbedding(Long spotId) {
        return getEmbeddingById(spotId)
                .map(SpotEmbedding::getTagEmbeddingAsString)
                .filter(tag -> tag != null && !tag.isEmpty());
    }

    public List<SpotEmbedding> getEmbeddingsBySpotIds(List<Long> spotIds) {
        return spotEmbeddingRepository.findBySpotIds(spotIds);
    }

    public Mono<SpotEmbedding> findBySpotId(Long spotId) {
        return Mono.fromCallable(() -> spotEmbeddingRepository.findBySpotId(spotId))
                .flatMap(optional -> optional.map(Mono::just).orElse(Mono.empty()))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
