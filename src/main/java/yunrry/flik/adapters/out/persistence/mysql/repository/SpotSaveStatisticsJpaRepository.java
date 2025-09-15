package yunrry.flik.adapters.out.persistence.mysql.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import yunrry.flik.adapters.out.persistence.mysql.entity.SpotSaveStatisticsEntity;

@Repository
public interface SpotSaveStatisticsJpaRepository extends JpaRepository<SpotSaveStatisticsEntity, Long> {

    /**
     * 장소의 저장 횟수 증가
     */
    @Modifying
    @Query("UPDATE SpotSaveStatisticsEntity s SET s.saveCount = s.saveCount + 1, " +
            "s.updatedAt = CURRENT_TIMESTAMP WHERE s.spotId = :spotId")
    int incrementSaveCount(@Param("spotId") Long spotId);

    /**
     * 장소의 저장 횟수 조회
     */
    @Query("SELECT COALESCE(s.saveCount, 0) FROM SpotSaveStatisticsEntity s WHERE s.spotId = :spotId")
    Integer findSaveCountBySpotId(@Param("spotId") Long spotId);


    SpotSaveStatisticsEntity findBySpotId(Long spotId);
}