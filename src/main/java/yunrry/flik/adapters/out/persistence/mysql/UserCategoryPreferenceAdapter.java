package yunrry.flik.adapters.out.persistence.mysql;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import yunrry.flik.adapters.out.persistence.mysql.entity.UserCategoryPreferenceEntity;
import yunrry.flik.adapters.out.persistence.mysql.repository.UserCategoryPreferenceJpaRepository;
import yunrry.flik.core.domain.model.UserCategoryPreference;
import yunrry.flik.ports.out.repository.UserCategoryPreferenceRepository;

import java.util.List;
import java.util.stream.Collectors;

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
            int updatedRows = userCategoryPreferenceJpaRepository
                    .incrementPreferenceScore(userId, detailCategory, increment);

            if (updatedRows == 0) {
                UserCategoryPreferenceEntity newEntity = UserCategoryPreferenceEntity.of(userId, detailCategory);
                newEntity.incrementPreference(increment);
                userCategoryPreferenceJpaRepository.save(newEntity);
                log.debug("Created new category preference - userId: {}, category: {}", userId, detailCategory);
            }
        } catch (Exception e) {
            log.error("Failed to increment preference score - userId: {}, category: {}", userId, detailCategory, e);
            throw e;
        }
    }

    @Override
    public double getPreferenceScore(Long userId, String detailCategory) {
        return userCategoryPreferenceJpaRepository
                .findByUserIdAndDetailCategory(userId, detailCategory)
                .map(entity -> entity.toDomain().getPreferenceScore())
                .orElse(0.0);
    }

    @Override
    public List<UserCategoryPreference> findByUserIdAndMainCategoryIn(Long userId, List<String> mainCategories) {
        return userCategoryPreferenceJpaRepository.findByUserIdAndMainCategoryIn(userId, mainCategories)
                .stream()
                .map(UserCategoryPreferenceEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Integer sumSaveCountByUserIdAndMainCategory(Long userId, String mainCategory) {
        return userCategoryPreferenceJpaRepository.sumSaveCountByUserIdAndMainCategory(userId, mainCategory);
    }

    @Override
    public UserCategoryPreference save(UserCategoryPreference userCategoryPreference) {
        UserCategoryPreferenceEntity entity = UserCategoryPreferenceEntity.fromDomain(userCategoryPreference);
        UserCategoryPreferenceEntity saved = userCategoryPreferenceJpaRepository.save(entity);
        return saved.toDomain();
    }
}