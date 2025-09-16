package yunrry.flik.ports.out.repository;

import yunrry.flik.core.domain.model.embedding.SpotEmbedding;
import java.util.List;
import java.util.Optional;

public interface SpotEmbeddingRepository {

    Optional<SpotEmbedding> findBySpotId(Long spotId);

    SpotEmbedding save(SpotEmbedding spotEmbedding);

    List<SpotEmbedding> findBySpotIds(List<Long> spotIds);

    List<Long> findSimilarSpots(List<Long> spotIds,
                                String locationVector,
                                String tagVector,
                                double locationWeight,
                                double tagWeight,
                                int limit);

    List<SpotEmbedding> findIncompleteEmbeddings();

    void deleteBySpotId(Long spotId);

    List<SpotEmbedding> findAll();
}