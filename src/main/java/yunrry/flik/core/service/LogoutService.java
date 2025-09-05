// core/service/LogoutService.java
package yunrry.flik.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yunrry.flik.ports.in.usecase.LogoutUseCase;
import yunrry.flik.ports.out.repository.RefreshTokenRepository;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void logout(String refreshToken) {
        if (jwtTokenProvider.validateToken(refreshToken)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            refreshTokenRepository.deleteByUserId(userId);
        }
    }
}