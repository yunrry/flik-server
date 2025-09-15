package yunrry.flik.adapters.out.persistence.mysql;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import yunrry.flik.adapters.out.persistence.mysql.entity.SpotSaveStatisticsEntity;
import yunrry.flik.adapters.out.persistence.mysql.repository.SpotSaveStatisticsJpaRepository;
import yunrry.flik.ports.out.repository.SpotSaveStatisticsRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SpotSaveStatisticsAdapter implements SpotSaveStatisticsRepository {

    private final SpotSaveStatisticsJpaRepository spotSaveStatisticsJpaRepository;

    @Override
    @Transactional
    public void incrementSaveCount(Long spotId) {
        if (spotId == null) {
            log.warn("Cannot increment save count: spotId is null");
            throw new IllegalArgumentException("Spot ID cannot be null");
        }

        try {
            // 기존 통계 레코드 업데이트 시도
            int updatedRows = spotSaveStatisticsJpaRepository.incrementSaveCount(spotId);

            // 업데이트된 행이 없으면 새로 생성
            if (updatedRows == 0) {
                SpotSaveStatisticsEntity newEntity = SpotSaveStatisticsEntity.of(spotId);
                newEntity.incrementSaveCount();
                spotSaveStatisticsJpaRepository.save(newEntity);
                log.debug("Created new spot statistics - spotId: {}, saveCount: 1", spotId);
            } else {
                log.debug("Updated spot statistics - spotId: {}", spotId);
            }
        } catch (Exception e) {
            log.error("Failed to increment save count for spot: {}", spotId, e);
            throw e;
        }
    }

    @Override
    public int getSaveCount(Long spotId) {
        if (spotId == null) {
            log.warn("Cannot get save count: spotId is null");
            return 0;
        }

        try {
            Integer saveCount = spotSaveStatisticsJpaRepository.findSaveCountBySpotId(spotId);
            return saveCount != null ? saveCount : 0;
        } catch (Exception e) {
            log.error("Failed to get save count for spotId: {}", spotId, e);
            return 0;
        }
    }

    @Override
    public Optional<SpotSaveStatisticsEntity> findBySpotId(Long spotId) {
        if (spotId == null) {
            log.warn("Spot ID cannot be null");
            return Optional.empty();
        }

        try {
            SpotSaveStatisticsEntity entity = spotSaveStatisticsJpaRepository.findBySpotId(spotId);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            log.error("Failed to find spot statistics for spotId: {}", spotId, e);
            return Optional.empty();
        }
    }
}