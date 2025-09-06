// core/service/OAuthSignupService.java
package yunrry.flik.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.exception.EmailAlreadyExistsException;
import yunrry.flik.core.domain.model.OAuthUserInfo;
import yunrry.flik.core.domain.model.RefreshToken;
import yunrry.flik.core.domain.model.User;
import yunrry.flik.ports.in.command.CompleteOAuthSignupCommand;
import yunrry.flik.ports.in.command.OAuthLoginCommand;
import yunrry.flik.ports.in.usecase.*;
import yunrry.flik.ports.out.repository.RefreshTokenRepository;
import yunrry.flik.ports.out.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthSignupService implements OAuthSignupUseCase {

    private final OAuth2Service oAuth2Service;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public OAuthUserInfo getOAuthUserInfo(OAuthLoginCommand command) {
        command.validate();

        return oAuth2Service.getUserInfo(
                command.getProvider(),
                command.getCode(),
                command.getState()
        );
    }

    @Override
    public AuthTokens completeOAuthSignup(CompleteOAuthSignupCommand command) {
        command.validate();

        // OAuth 사용자 정보 다시 조회
        OAuthUserInfo oAuthUserInfo = command.getOAuthUserInfo();

        // 이미 가입된 사용자 확인 (이메일 또는 providerId로)
        Optional<User> existingUser = userRepository.findByEmail(oAuthUserInfo.getEmail());
        if (existingUser.isPresent()) {
            throw new EmailAlreadyExistsException(oAuthUserInfo.getEmail());
        }

        Optional<User> existingOAuthUser = userRepository.findByProviderAndProviderId(
                oAuthUserInfo.getProvider(),
                oAuthUserInfo.getProviderId()
        );
        if (existingOAuthUser.isPresent()) {
            // 이미 가입된 OAuth 사용자면 바로 로그인 처리
            return createAuthTokens(existingOAuthUser.get());
        }

        // 새 사용자 생성 (사용자가 입력한 닉네임 사용)
        User newUser = User.builder()
                .email(oAuthUserInfo.getEmail())
                .password(null)  // OAuth 사용자는 비밀번호 없음
                .nickname(command.getNickname())  // 사용자 입력 닉네임
                .profileImageUrl(oAuthUserInfo.getProfileImageUrl())
                .authProvider(oAuthUserInfo.getProvider())
                .providerId(oAuthUserInfo.getProviderId())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(newUser);
        return createAuthTokens(savedUser);
    }

    private AuthTokens createAuthTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        RefreshToken refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenRepository.save(refreshToken);

        return AuthTokens.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(user)
                .build();
    }
}