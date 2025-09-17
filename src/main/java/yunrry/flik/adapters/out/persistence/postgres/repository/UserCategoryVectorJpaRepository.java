package yunrry.flik.adapters.out.persistence.postgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yunrry.flik.adapters.out.persistence.postgres.entity.UserCategoryVectorEntity;

import java.util.List;
import java.util.Optional;

public interface UserCategoryVectorJpaRepository extends JpaRepository<UserCategoryVectorEntity, Long> {

    Optional<UserCategoryVectorEntity> findByUserIdAndCategory(Long userId, String category);

    List<UserCategoryVectorEntity> findByUserId(Long userId);

    @Modifying
    void deleteByUserId(Long userId);

    @Modifying
    void deleteByUserIdAndCategory(Long userId, String category);

    @Query("SELECT ucv FROM UserCategoryVectorEntity ucv WHERE ucv.category = :category")
    List<UserCategoryVectorEntity> findByCategory(@Param("category") String category);

    boolean existsByUserIdAndCategory(Long userId, String category);

    // 벡터만 조회 (성능 최적화)
    @Query("SELECT ucv.preferenceVector FROM UserCategoryVectorEntity ucv WHERE ucv.userId = :userId AND ucv.category = :category")
    Optional<List<Double>> findPreferenceVectorByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);


    // 벡터 업데이트 (PostgreSQL 네이티브)
    @Modifying
    @Query(value = """
        WITH favorite_vectors AS (
            SELECT AVG(se.tag_embedding) as avg_vector, COUNT(*) as spot_count
            FROM spot_embeddings se
            WHERE se.spot_id = ANY(:spotIds)
              AND se.tag_embedding IS NOT NULL
        ),
        existing_vector AS (
            SELECT preference_vector, preference_count
            FROM user_category_vectors
            WHERE user_id = :userId AND category = :category
        )
        INSERT INTO user_category_vectors (user_id, category, preference_vector, preference_count)
        SELECT 
            :userId, :category, 
            CASE 
                WHEN ev.preference_vector IS NULL THEN fv.avg_vector
                ELSE (ev.preference_vector * ev.preference_count + fv.avg_vector * fv.spot_count) 
                     / (ev.preference_count + fv.spot_count)
            END as new_vector,
            COALESCE(ev.preference_count, 0) + fv.spot_count as new_count
        FROM favorite_vectors fv
        LEFT JOIN existing_vector ev ON true
        WHERE fv.spot_count > 0
        ON CONFLICT (user_id, category)
        DO UPDATE SET
            preference_vector = EXCLUDED.preference_vector,
            preference_count = EXCLUDED.preference_count,
            updated_at = CURRENT_TIMESTAMP
        """, nativeQuery = true)
    int updateUserPreferenceVector(@Param("userId") Long userId,
                                   @Param("category") String category,
                                   @Param("spotIds") Long[] spotIds);

    // 카테고리별 벡터 재계산
    @Modifying
    @Query(value = """
    WITH avg_vector AS (
        SELECT AVG(se.tag_embedding) as preference_vector, COUNT(*) as spot_count
        FROM spot_embeddings se
        WHERE se.spot_id = ANY(:spotIds)
          AND se.tag_embedding IS NOT NULL
    )
    INSERT INTO user_category_vectors (user_id, category, preference_vector, preference_count)
    SELECT :userId, :category, preference_vector, spot_count
    FROM avg_vector
    WHERE spot_count > 0
    ON CONFLICT (user_id, category)
    DO UPDATE SET
        preference_vector = EXCLUDED.preference_vector,
        preference_count = EXCLUDED.preference_count,
        updated_at = CURRENT_TIMESTAMP
    """, nativeQuery = true)
    int recalculateCategoryVector(@Param("userId") Long userId,
                                  @Param("category") String category,
                                  @Param("spotIds") Long[] spotIds);

    // 사용자 벡터 통계
    @Query("SELECT COUNT(ucv) FROM UserCategoryVectorEntity ucv WHERE ucv.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    // 카테고리별 사용자 수 조회
    @Query("SELECT COUNT(DISTINCT ucv.userId) FROM UserCategoryVectorEntity ucv WHERE ucv.category = :category")
    long countUsersByCategory(@Param("category") String category);

    // 벡터가 있는 사용자들만 조회
    @Query("SELECT ucv FROM UserCategoryVectorEntity ucv WHERE ucv.preferenceVector IS NOT NULL")
    List<UserCategoryVectorEntity> findAllWithVectors();
}