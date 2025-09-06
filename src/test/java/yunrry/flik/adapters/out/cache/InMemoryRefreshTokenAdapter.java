package yunrry.flik.adapters.out.cache;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import yunrry.flik.core.domain.model.RefreshToken;
import yunrry.flik.ports.out.repository.RefreshTokenRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("test")
public class InMemoryRefreshTokenAdapter implements RefreshTokenRepository {

    private final Map<Long, RefreshToken> tokens = new ConcurrentHashMap<>();

    @Override
    public void save(RefreshToken refreshToken) {
        tokens.put(refreshToken.getUserId(), refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByUserId(Long userId) {
        return Optional.ofNullable(tokens.get(userId));
    }

    @Override
    public void deleteByUserId(Long userId) {
        tokens.remove(userId);
    }

    @Override
    public void deleteByToken(String token) {
        tokens.entrySet().removeIf(entry ->
                entry.getValue().getToken().equals(token));
    }
}