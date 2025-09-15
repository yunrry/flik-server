package yunrry.flik.adapters.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import yunrry.flik.adapters.out.persistence.entity.UserSavedSpotEntity;

@Repository
public interface UserSavedSpotJpaRepository extends JpaRepository<UserSavedSpotEntity, Long> {

    /**
     * 사용자가 특정 장소를 저장했는지 확인
     */
    boolean existsByUserIdAndSpotId(Long userId, Long spotId);

    /**
     * 사용자가 저장한 특정 장소 삭제
     */
    @Modifying
    @Query("DELETE FROM UserSavedSpotEntity u WHERE u.userId = :userId AND u.spotId = :spotId")
    void deleteByUserIdAndSpotId(@Param("userId") Long userId, @Param("spotId") Long spotId);


    @Query("SELECT COUNT(u) FROM UserSavedSpotEntity u WHERE u.userId = :userId AND u.spotId = :spotId")
    int countByUserIdAndSpotId(Long userId, Long spotId);


    @Query("SELECT COUNT(u) FROM UserSavedSpotEntity u WHERE u.spotId = :spotId")
    int countBySpotId(Long spotId);
}