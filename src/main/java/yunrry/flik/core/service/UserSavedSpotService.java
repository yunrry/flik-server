package yunrry.flik.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import yunrry.flik.core.service.UserSavedSpotService;
import yunrry.flik.ports.in.usecase.UserSavedSpotUseCase;
import yunrry.flik.ports.out.repository.UserSavedSpotRepository;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserSavedSpotService implements UserSavedSpotUseCase {

    private final UserSavedSpotRepository userSavedSpotRepository;

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

        userSavedSpotRepository.save(userId, spotId);
        log.info("User spot saved - userId: {}, spotId: {}", userId, spotId);
    }

    @Override
    @Transactional
    public void removeUserSpot(Long userId, Long spotId) {
        userSavedSpotRepository.deleteByUserIdAndSpotId(userId, spotId);
        log.info("User spot removed - userId: {}, spotId: {}", userId, spotId);
    }
}