package yunrry.flik.ports.out.repository;

import yunrry.flik.core.domain.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {
    void save(RefreshToken refreshToken);
    Optional<RefreshToken> findByUserId(Long userId);
    void deleteByUserId(Long userId);
    void deleteByToken(String token);
}