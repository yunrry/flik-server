package yunrry.flik.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.exception.InvalidPasswordException;
import yunrry.flik.core.domain.exception.OAuthSignupRequiredException;
import yunrry.flik.core.domain.exception.UserNotFoundException;
import yunrry.flik.core.domain.model.OAuthUserInfo;
import yunrry.flik.core.domain.model.RefreshToken;
import yunrry.flik.core.domain.model.User;
import yunrry.flik.ports.in.command.LoginCommand;
import yunrry.flik.ports.in.command.OAuthLoginCommand;
import yunrry.flik.ports.in.usecase.*;
import yunrry.flik.ports.out.repository.RefreshTokenRepository;
import yunrry.flik.ports.out.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2Service oAuth2Service;

    @Override
    public AuthTokens login(LoginCommand command) {
        command.validate();

        User user = userRepository.findByEmail(command.getEmail())
                .orElseThrow(() -> new UserNotFoundException(command.getEmail()));

        if (!user.canLogin()) {
            throw new RuntimeException("비활성화된 사용자입니다");
        }

        if (!passwordEncoder.matches(command.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        return createAuthTokens(user);
    }

    @Override
    public AuthTokens oauthLogin(OAuthLoginCommand command) {
        command.validate();

        OAuthUserInfo oAuthUserInfo = oAuth2Service.getUserInfo(
                command.getProvider(),
                command.getCode(),
                command.getState()
        );

        // 기존 사용자 확인 (providerId로만 확인)
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(
                command.getProvider(),
                oAuthUserInfo.getProviderId()
        );

        if (existingUser.isEmpty()) {
            // 첫 로그인이면 회원가입 필요 예외 발생
            throw new OAuthSignupRequiredException(oAuthUserInfo);
        }

        User user = existingUser.get();
        return createAuthTokens(user);
    }

    private AuthTokens createAuthTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        RefreshToken refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenRepository.save(refreshToken);

        User updatedUser = user.updateLastLogin();
        userRepository.save(updatedUser);

        return AuthTokens.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(updatedUser)
                .build();
    }
}