package yunrry.flik.ports.out.repository;

import yunrry.flik.core.domain.model.AuthProvider;
import yunrry.flik.core.domain.model.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
    boolean existsByEmail(String email);
    void deleteById(Long id);
    String findNickNameById(Long id);
}