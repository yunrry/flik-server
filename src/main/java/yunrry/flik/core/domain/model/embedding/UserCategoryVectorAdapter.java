package yunrry.flik.core.domain.model.embedding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import yunrry.flik.adapters.out.persistence.postgres.entity.UserCategoryVectorEntity;
import yunrry.flik.adapters.out.persistence.postgres.repository.UserCategoryVectorJpaRepository;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.UserCategoryVector;
import yunrry.flik.ports.out.repository.UserCategoryVectorRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserCategoryVectorAdapter implements UserCategoryVectorRepository {

    private final UserCategoryVectorJpaRepository userCategoryVectorJpaRepository;

    @Override
    @Transactional
    public void saveUserCategoryVectors(Long userId, Map<MainCategory, String> categoryVectors) {
        for (Map.Entry<MainCategory, String> entry : categoryVectors.entrySet()) {
            MainCategory category = entry.getKey();
            String vector = entry.getValue();

            Optional<UserCategoryVectorEntity> existing = 
                userCategoryVectorJpaRepository.findByUserIdAndCategory(userId, category.getCode());

            if (existing.isPresent()) {
                existing.get().updateVector(vector);
                userCategoryVectorJpaRepository.save(existing.get());
            } else {
                UserCategoryVectorEntity newEntity = UserCategoryVectorEntity.builder()
                    .userId(userId)
                    .category(category.getCode())
                    .preferenceVector(vector)
                    .build();
                userCategoryVectorJpaRepository.save(newEntity);
            }
        }
        log.info("Saved {} category vectors for user: {}", categoryVectors.size(), userId);
    }

    @Override
    public Optional<String> getUserCategoryVector(Long userId, MainCategory category) {
        return userCategoryVectorJpaRepository.findByUserIdAndCategory(userId, category.getCode())
            .map(UserCategoryVectorEntity::getPreferenceVector);
    }

    @Override
    public String getDefaultCategoryVector(MainCategory category) {
        // 1536차원 기본 벡터 (모든 값이 0.0)
        List<Double> defaultVector = Collections.nCopies(1536, 0.0);
        return "[" + defaultVector.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(",")) + "]";
    }

    @Override
    @Transactional
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
    @Transactional
    public UserCategoryVector save(UserCategoryVector userCategoryVector) {
        UserCategoryVectorEntity entity;
        
        if (userCategoryVector.getId() != null) {
            entity = userCategoryVectorJpaRepository.findById(userCategoryVector.getId())
                .orElse(UserCategoryVectorEntity.fromDomain(userCategoryVector));
            entity.updateVector(userCategoryVector.getPreferenceVectorAsString());
        } else {
            entity = UserCategoryVectorEntity.fromDomain(userCategoryVector);
        }
        
        UserCategoryVectorEntity savedEntity = userCategoryVectorJpaRepository.save(entity);
        log.debug("Saved category vector for user: {}, category: {}", 
            savedEntity.getUserId(), savedEntity.getCategory());
        
        return savedEntity.toDomain();
    }
}