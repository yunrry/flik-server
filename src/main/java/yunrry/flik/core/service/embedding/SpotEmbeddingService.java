package yunrry.flik.core.service.embedding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import yunrry.flik.core.domain.model.embedding.SpotSimilarity;
import yunrry.flik.core.domain.model.embedding.SpotEmbedding;
import yunrry.flik.ports.out.repository.SpotEmbeddingRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotEmbeddingService {

    private final SpotEmbeddingRepository spotEmbeddingRepository;


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
        return Mono.fromCallable(() -> spotEmbeddingRepository.save(spotEmbedding))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(saved -> log.info("Saved embedding for spot: {}", saved.getSpotId()));
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
}
