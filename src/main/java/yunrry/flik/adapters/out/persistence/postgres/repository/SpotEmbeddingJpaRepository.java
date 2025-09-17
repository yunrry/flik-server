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

    @Query("SELECT se FROM SpotEmbeddingEntity se WHERE se.tagEmbedding IS NULL OR se.locationEmbedding IS NULL")
    List<SpotEmbeddingEntity> findIncompleteEmbeddings();

    void deleteBySpotId(Long spotId);

    // 단순 벡터 유사도 검색용 네이티브 쿼리
    @Query(value = """
        SELECT 
            se.spot_id,
            (1 - (ucv.preference_vector <=> se.tag_embedding)) as similarity
        FROM spot_embeddings se
        JOIN user_category_vectors ucv ON ucv.user_id = :userId AND ucv.category = :category
        WHERE se.spot_id = ANY(:spotIds)
          AND se.tag_embedding IS NOT NULL
          AND ucv.preference_vector IS NOT NULL
        ORDER BY similarity DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findSimilarSpotsByUserPreference(@Param("userId") Long userId,
                                                    @Param("category") String category,
                                                    @Param("spotIds") Long[] spotIds,
                                                    @Param("limit") int limit);

    // tag embedding이 있는 spot_id들만 조회
    @Query("SELECT se.spotId FROM SpotEmbeddingEntity se WHERE se.spotId IN :spotIds AND se.tagEmbedding IS NOT NULL")
    List<Long> findSpotIdsWithTagEmbedding(@Param("spotIds") List<Long> spotIds);

    // tag embedding이 없는 spot_id들 조회
    @Query("SELECT se.spotId FROM SpotEmbeddingEntity se WHERE se.spotId IN :spotIds AND se.tagEmbedding IS NULL")
    List<Long> findSpotIdsWithoutTagEmbedding(@Param("spotIds") List<Long> spotIds);

    // 통계용 쿼리들
    @Query("SELECT COUNT(se) FROM SpotEmbeddingEntity se WHERE se.tagEmbedding IS NOT NULL")
    long countSpotsWithTagEmbedding();

    @Query("SELECT COUNT(se) FROM SpotEmbeddingEntity se WHERE se.locationEmbedding IS NOT NULL")
    long countSpotsWithLocationEmbedding();

    boolean existsBySpotId(Long spotId);
}