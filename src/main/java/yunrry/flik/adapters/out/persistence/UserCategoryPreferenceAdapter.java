package yunrry.flik.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import yunrry.flik.adapters.out.persistence.entity.UserCategoryPreferenceEntity;
import yunrry.flik.adapters.out.persistence.repository.UserCategoryPreferenceJpaRepository;
import yunrry.flik.ports.out.repository.UserCategoryPreferenceRepository;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserCategoryPreferenceAdapter implements UserCategoryPreferenceRepository {

    private final UserCategoryPreferenceJpaRepository userCategoryPreferenceJpaRepository;

    @Override
    @Transactional
    public void incrementPreferenceScore(Long userId, String detailCategory, Double increment) {
        try {
            // 기존 선호도 레코드 업데이트 시도
            int updatedRows = userCategoryPreferenceJpaRepository
                    .incrementPreferenceScore(userId, detailCategory, increment);

            // 업데이트된 행이 없으면 새로 생성
            if (updatedRows == 0) {
                UserCategoryPreferenceEntity newEntity = UserCategoryPreferenceEntity.of(userId, detailCategory);
                newEntity.incrementPreference(increment);
                userCategoryPreferenceJpaRepository.save(newEntity);
                log.debug("Created new category preference - userId: {}, category: {}, score: {}",
                        userId, detailCategory, increment);
            } else {
                log.debug("Updated category preference - userId: {}, category: {}, increment: {}",
                        userId, detailCategory, increment);
            }
        } catch (Exception e) {
            log.error("Failed to increment preference score - userId: {}, category: {}, increment: {}",
                    userId, detailCategory, increment, e);
            throw e;
        }
    }

    @Override
    public double getPreferenceScore(Long userId, String detailCategory) {
        return userCategoryPreferenceJpaRepository
                .findByUserIdAndDetailCategory(userId, detailCategory)
                .map(UserCategoryPreferenceEntity::getPreferenceScore)
                .orElse(0.0);
    }
}