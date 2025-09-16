package yunrry.flik.adapters.out.persistence.postgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yunrry.flik.adapters.out.persistence.postgres.entity.UserCategoryVectorEntity;

import java.util.List;
import java.util.Optional;

public interface UserCategoryVectorJpaRepository extends JpaRepository<UserCategoryVectorEntity, Long> {

    Optional<UserCategoryVectorEntity> findByUserIdAndCategory(Long userId, String category);

    List<UserCategoryVectorEntity> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    void deleteByUserIdAndCategory(Long userId, String category);

    @Query("SELECT ucv FROM UserCategoryVectorEntity ucv WHERE ucv.category = :category")
    List<UserCategoryVectorEntity> findByCategory(@Param("category") String category);

    boolean existsByUserIdAndCategory(Long userId, String category);
}