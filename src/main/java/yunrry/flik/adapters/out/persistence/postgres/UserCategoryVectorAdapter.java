package yunrry.flik.adapters.out.persistence.postgres;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import yunrry.flik.adapters.out.persistence.postgres.entity.UserCategoryVectorEntity;
import yunrry.flik.adapters.out.persistence.postgres.repository.UserCategoryVectorJpaRepository;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.UserCategoryVector;
import yunrry.flik.core.domain.model.embedding.UserVectorStats;
import yunrry.flik.ports.out.repository.UserCategoryVectorRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCategoryVectorAdapter implements UserCategoryVectorRepository {

    private final UserCategoryVectorJpaRepository userCategoryVectorJpaRepository;

    @Override
    public void saveUserCategoryVectors(Long userId, Map<MainCategory, List<Double>> categoryVectors) {
        for (Map.Entry<MainCategory, List<Double>> entry : categoryVectors.entrySet()) {
            MainCategory category = entry.getKey();
            List<Double> vector = entry.getValue();

            Optional<UserCategoryVectorEntity> existing =
                    userCategoryVectorJpaRepository.findByUserIdAndCategory(userId, category.getCode());

            if (existing.isPresent()) {
                existing.get().updateVector(vector, null);
                userCategoryVectorJpaRepository.save(existing.get());
            } else {
                UserCategoryVectorEntity newEntity = UserCategoryVectorEntity.builder()
                        .userId(userId)
                        .category(category.getCode())
                        .preferenceVector(vector)
                        .preferenceCount(1)
                        .build();
                userCategoryVectorJpaRepository.save(newEntity);
            }
        }
        log.info("Saved {} category vectors for user: {}", categoryVectors.size(), userId);
    }

    @Override
    public Optional<List<Double>> getUserCategoryVector(Long userId, MainCategory category) {
        return userCategoryVectorJpaRepository.findPreferenceVectorByUserIdAndCategory(userId, category.getCode());
    }

    @Override
    public List<Double> getDefaultCategoryVector(MainCategory category) {
        return Collections.nCopies(1536, 0.0);
    }

    @Override
    public void deleteUserCategoryVectors(Long userId) {
        userCategoryVectorJpaRepository.deleteByUserId(userId);
        log.info("Deleted all category vectors for user: {}", userId);
    }

    @Override
    public Optional<UserCategoryVector> findByUserIdAndCategory(Long userId, MainCategory category) {
        return userCategoryVectorJpaRepository.findByUserIdAndCategory(userId, category.getCode())
                .map(UserCategoryVectorEntity::toDomain);
    }

    @Override
    public List<UserCategoryVector> findByUserId(Long userId) {
        return userCategoryVectorJpaRepository.findByUserId(userId).stream()
                .map(UserCategoryVectorEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public UserCategoryVector save(UserCategoryVector userCategoryVector) {
        // upsert 방식으로 처리
        Optional<UserCategoryVectorEntity> existing = userCategoryVectorJpaRepository
                .findByUserIdAndCategory(userCategoryVector.getUserId(), userCategoryVector.getCategory().getCode());

        UserCategoryVectorEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.updateVector(
                    userCategoryVector.getPreferenceVector(),
                    userCategoryVector.getPreferenceCount()
            );
        } else {
            entity = UserCategoryVectorEntity.fromDomain(userCategoryVector);
        }

        UserCategoryVectorEntity savedEntity = userCategoryVectorJpaRepository.save(entity);
        log.debug("Saved category vector for user: {}, category: {}",
                savedEntity.getUserId(), savedEntity.getCategory());

        return savedEntity.toDomain();
    }

    @Override
    public void updateUserPreferenceVector(Long userId, MainCategory category, List<Long> newFavoriteSpotIds) {
        // 이 메서드는 PostgreSQL 함수를 사용하도록 별도 서비스에서 구현
        int updated = userCategoryVectorJpaRepository.updateUserPreferenceVector(userId, category.getCode(), newFavoriteSpotIds.toArray(Long[]::new));
        log.info("Updated preference vector: user={}, category={}, spots={}, result={}", userId, category, newFavoriteSpotIds.size(), updated > 0);
    }


    @Override
    public void recalculateCategoryVector(Long userId, MainCategory category, List<Long> SpotIds) {
        // 이 메서드는 PostgreSQL 함수를 사용하도록 별도 서비스에서 구현
        int updated = userCategoryVectorJpaRepository.recalculateCategoryVector(userId, category.getCode(), SpotIds.toArray(Long[]::new));
        log.info("recalculate preference vector: user={}, category={}, spots={}, result={}", userId, category, SpotIds.size(), updated > 0);
    }



    @Override
    public UserVectorStats getUserVectorStats(Long userId) {
        long vectorCount = userCategoryVectorJpaRepository.countByUserId(userId);
        long totalCategories = MainCategory.values().length;

        return new UserVectorStats(
                userId,
                vectorCount,
                totalCategories,
                vectorCount > 0 ? (double) vectorCount / totalCategories * 100 : 0.0
        );
    }

    @Override
    public boolean existsByUserIdAndCategory(Long userId, MainCategory category) {
        return userCategoryVectorJpaRepository.existsByUserIdAndCategory(userId, category.getCode());
    }


}