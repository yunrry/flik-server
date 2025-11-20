package yunrry.flik.core.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import yunrry.flik.core.service.MetricsService;
import yunrry.flik.ports.in.usecase.UserSavedSpotUseCase;
import yunrry.flik.ports.out.repository.UserSavedSpotRepository;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserSavedSpotService implements UserSavedSpotUseCase {
    private final UserCategoryVectorService userCategoryVectorService;
    private final UserSavedSpotRepository userSavedSpotRepository;
    private final MetricsService metricsService;

    @Override
    public boolean isAlreadySaved(Long userId, Long spotId) {
        boolean exists = userSavedSpotRepository.existsByUserIdAndSpotId(userId, spotId);
        log.debug("Checking if spot is already saved - userId: {}, spotId: {}, exists: {}",
                userId, spotId, exists);
        return exists;
    }

    @Override
    @Transactional
    public void saveUserSpot(Long userId, Long spotId) {
        if (isAlreadySaved(userId, spotId)) {
            log.warn("Attempting to save already saved spot - userId: {}, spotId: {}", userId, spotId);
            throw new IllegalStateException("Spot already saved by user");
        }

        // 스팟 저장 메트릭 기록
        metricsService.incrementSpotSave();

        userSavedSpotRepository.save(userId, spotId);
        // 벡터 업데이트
        log.info("Calling updateVectorForSavedSpot for userId: {}, spotId: {}", userId, spotId);
        userCategoryVectorService.updateVectorForSavedSpot(userId, spotId);

        log.info("User spot saved - userId: {}, spotId: {}", userId, spotId);
    }

    @Override
    @Transactional
    public void removeUserSpot(Long userId, Long spotId) {
        userSavedSpotRepository.deleteByUserIdAndSpotId(userId, spotId);
        log.info("User spot removed - userId: {}, spotId: {}", userId, spotId);
    }

    public Mono<List<Long>> getUserSavedSpotIds(Long userId) {
        return Mono.fromCallable(() -> {
                    return userSavedSpotRepository.findSpotIdsByUserId(userId);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}