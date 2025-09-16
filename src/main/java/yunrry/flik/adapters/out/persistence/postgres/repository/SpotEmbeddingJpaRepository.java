package yunrry.flik.adapters.out.persistence.postgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yunrry.flik.adapters.out.persistence.postgres.entity.SpotEmbeddingEntity;

import java.util.List;
import java.util.Optional;

public interface SpotEmbeddingJpaRepository extends JpaRepository<SpotEmbeddingEntity, Long> {

    Optional<SpotEmbeddingEntity> findBySpotId(Long spotId);

    List<SpotEmbeddingEntity> findBySpotIdIn(List<Long> spotIds);

    @Query(value = """
        SELECT se.*, 
               (se.location_embedding <=> CAST(:locationVector AS vector)) as location_distance,
               (se.tag_embedding <=> CAST(:tagVector AS vector)) as tag_distance
        FROM spot_embeddings se 
        WHERE se.spot_id IN :spotIds
        ORDER BY (
            :locationWeight * (se.location_embedding <=> CAST(:locationVector AS vector)) + 
            :tagWeight * (se.tag_embedding <=> CAST(:tagVector AS vector))
        )
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findSimilarSpots(@Param("spotIds") List<Long> spotIds,
                                    @Param("locationVector") String locationVector,
                                    @Param("tagVector") String tagVector,
                                    @Param("locationWeight") double locationWeight,
                                    @Param("tagWeight") double tagWeight,
                                    @Param("limit") int limit);

    @Query("SELECT se FROM SpotEmbeddingEntity se WHERE se.tagEmbedding IS NULL OR se.locationEmbedding IS NULL")
    List<SpotEmbeddingEntity> findIncompleteEmbeddings();

    void deleteBySpotId(Long spotId);
}