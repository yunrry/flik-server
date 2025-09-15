package yunrry.flik.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import yunrry.flik.adapters.out.persistence.entity.UserSavedSpotEntity;
import yunrry.flik.adapters.out.persistence.repository.UserSavedSpotJpaRepository;
import yunrry.flik.ports.out.repository.UserSavedSpotRepository;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserSavedSpotAdapter implements UserSavedSpotRepository {

    private final UserSavedSpotJpaRepository userSavedSpotJpaRepository;

    @Override
    public boolean existsByUserIdAndSpotId(Long userId, Long spotId) {
        return userSavedSpotJpaRepository.existsByUserIdAndSpotId(userId, spotId);
    }

    @Override
    @Transactional
    public void save(Long userId, Long spotId) {
        try {
            UserSavedSpotEntity entity = UserSavedSpotEntity.of(userId, spotId);
            userSavedSpotJpaRepository.save(entity);
            log.debug("UserSavedSpot saved successfully - userId: {}, spotId: {}", userId, spotId);
        } catch (DataIntegrityViolationException e) {
            // 중복 키 제약 조건 위반 시 (이미 저장된 경우)
            log.warn("Duplicate key violation when saving user spot - userId: {}, spotId: {}", userId, spotId);
            throw new IllegalStateException("이미 저장된 장소입니다.", e);
        }
    }

    @Override
    @Transactional
    public void deleteByUserIdAndSpotId(Long userId, Long spotId) {
        userSavedSpotJpaRepository.deleteByUserIdAndSpotId(userId, spotId);
        log.debug("UserSavedSpot deleted - userId: {}, spotId: {}", userId, spotId);
    }

    @Override
    public int countBySpotId(Long spotId) {
        return userSavedSpotJpaRepository.countBySpotId(spotId);
    }
}