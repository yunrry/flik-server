package yunrry.flik.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import yunrry.flik.core.service.SpotAnalyticsService;
import yunrry.flik.ports.in.usecase.SpotAnalyticsUseCase;
import yunrry.flik.ports.out.repository.SpotSaveStatisticsRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SpotAnalyticsService implements SpotAnalyticsUseCase {

    private final SpotSaveStatisticsRepository spotSaveStatisticsRepository;

    @Override
    @Transactional
    public void incrementSaveCount(Long spotId) {
        try {
            spotSaveStatisticsRepository.incrementSaveCount(spotId);
            log.debug("Incremented save count for spot: {}", spotId);
        } catch (Exception e) {
            log.error("Failed to increment save count for spot: {}", spotId, e);
            throw e;
        }
    }

    @Override
    public int getSaveCount(Long spotId) {
        return spotSaveStatisticsRepository.getSaveCount(spotId);
    }
}