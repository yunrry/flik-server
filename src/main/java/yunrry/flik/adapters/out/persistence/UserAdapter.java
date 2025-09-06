package yunrry.flik.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import yunrry.flik.adapters.out.persistence.entity.UserEntity;
import yunrry.flik.adapters.out.persistence.repository.UserJpaRepository;
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
        UserEntity entity = UserEntity.fromDomain(user);
        UserEntity savedEntity = userJpaRepository.save(entity);
        return savedEntity.toDomain();
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