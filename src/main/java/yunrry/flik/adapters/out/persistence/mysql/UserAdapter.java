package yunrry.flik.adapters.out.persistence.mysql;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Component;
import yunrry.flik.adapters.out.persistence.mysql.entity.UserEntity;
import yunrry.flik.adapters.out.persistence.mysql.repository.UserJpaRepository;
import yunrry.flik.core.domain.model.AuthProvider;
import yunrry.flik.core.domain.model.User;
import yunrry.flik.ports.out.repository.UserRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        try {
            UserEntity entity = UserEntity.fromDomain(user);
            UserEntity savedEntity = userJpaRepository.save(entity);

            if (savedEntity == null || savedEntity.getId() == null) {
                throw new RuntimeException("사용자 저장에 실패했습니다.");
            }

            // 저장된 엔티티 다시 조회하여 검증
            UserEntity verifiedEntity = userJpaRepository.findById(savedEntity.getId())
                    .orElseThrow(() -> new RuntimeException("사용자 저장 검증에 실패했습니다."));

            return verifiedEntity.toDomain();

        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("중복된 사용자 정보가 존재합니다: " + e.getMessage());
        } catch (JpaSystemException e) {
            throw new RuntimeException("데이터베이스 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id)
                .map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId) {
        return userJpaRepository.findByProviderAndProviderId(provider, providerId)
                .map(UserEntity::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(Long id) {
        userJpaRepository.deleteById(id);
    }
}