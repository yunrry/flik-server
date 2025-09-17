package yunrry.flik.ports.out.repository;

import yunrry.flik.core.domain.model.embedding.EmbeddingStats;
import yunrry.flik.core.domain.model.embedding.SpotSimilarity;
import yunrry.flik.core.domain.model.embedding.SpotEmbedding;
import java.util.List;
import java.util.Optional;

public interface SpotEmbeddingRepository {

    Optional<SpotEmbedding> findBySpotId(Long spotId);

    SpotEmbedding save(SpotEmbedding spotEmbedding);

    List<SpotEmbedding> findBySpotIds(List<Long> spotIds);

    List<SpotSimilarity> findSimilarSpotsByUserPreference(Long userId,
                                                          String category,
                                                          List<Long> spotIds,
                                                          int limit);

    List<SpotEmbedding> findIncompleteEmbeddings();

    void deleteBySpotId(Long spotId);

    List<SpotEmbedding> findAll();

    List<Long> findSpotIdsWithTagEmbedding(List<Long> spotIds);

    List<Long> findSpotIdsWithoutTagEmbedding(List<Long> spotIds);

    EmbeddingStats getEmbeddingStats();

    boolean existsBySpotId(Long spotId);
}