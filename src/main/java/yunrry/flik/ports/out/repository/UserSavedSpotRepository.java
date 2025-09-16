package yunrry.flik.ports.out.repository;

import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface UserSavedSpotRepository {

    /**
     * 사용자가 특정 장소를 저장했는지 확인
     */
    boolean existsByUserIdAndSpotId(Long userId, Long spotId);

    /**
     * 사용자 저장 장소 추가
     */
    void save(Long userId, Long spotId);

    /**
     * 사용자 저장 장소 삭제
     */
    void deleteByUserIdAndSpotId(Long userId, Long spotId);

    int countBySpotId(Long spotId);

    List<Long> findSpotIdsByUserId(@Param("userId") Long userId);
}