package yunrry.flik.adapters.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import yunrry.flik.adapters.out.persistence.entity.UserCategoryPreferenceEntity;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface UserCategoryPreferenceJpaRepository extends JpaRepository<UserCategoryPreferenceEntity, Long> {

    /**
     * 사용자의 특정 카테고리 선호도 조회
     */
    Optional<UserCategoryPreferenceEntity> findByUserIdAndDetailCategory(Long userId, String detailCategory);

    /**
     * 사용자의 특정 카테고리 선호도 점수 업데이트
     */
    @Modifying
    @Query("UPDATE UserCategoryPreferenceEntity u SET u.preferenceScore = u.preferenceScore + :increment, " +
            "u.saveCount = u.saveCount + 1, u.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE u.userId = :userId AND u.detailCategory = :category")
    int incrementPreferenceScore(@Param("userId") Long userId,
                                 @Param("category") String category,
                                 @Param("increment") Double increment);
}