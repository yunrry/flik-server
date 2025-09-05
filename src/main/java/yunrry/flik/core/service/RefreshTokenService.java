package yunrry.flik.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.exception.InvalidTokenException;
import yunrry.flik.core.domain.exception.UserNotFoundException;
import yunrry.flik.core.domain.model.RefreshToken;
import yunrry.flik.core.domain.model.User;
import yunrry.flik.ports.in.command.RefreshTokenCommand;
import yunrry.flik.ports.in.usecase.AuthTokens;
import yunrry.flik.ports.in.usecase.RefreshTokenUseCase;
import yunrry.flik.ports.out.repository.RefreshTokenRepository;
import yunrry.flik.ports.out.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public AuthTokens refreshTokens(RefreshTokenCommand command) {
        command.validate();

        if (!jwtTokenProvider.validateToken(command.getRefreshToken())) {
            throw new InvalidTokenException();
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(command.getRefreshToken());

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(InvalidTokenException::new);

        if (!refreshToken.isValid() || !refreshToken.getToken().equals(command.getRefreshToken())) {
            throw new InvalidTokenException();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 새로운 토큰 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        RefreshToken newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

        // 기존 리프레시 토큰 삭제하고 새로운 토큰 저장
        refreshTokenRepository.deleteByUserId(userId);
        refreshTokenRepository.save(newRefreshToken);

        return AuthTokens.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .user(user)
                .build();
    }
}