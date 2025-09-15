package yunrry.flik.ports.out.repository;

import java.util.Optional;

public interface SpotSaveStatisticsRepository {

    /**
     * 장소의 저장 횟수 증가
     */
    void incrementSaveCount(Long spotId);

    /**
     * 장소의 저장 횟수 조회
     */
    int getSaveCount(Long spotId);

    /**
     * 장소 ID로 통계 엔티티 조회
     */
    Optional findBySpotId(Long spotId);
}