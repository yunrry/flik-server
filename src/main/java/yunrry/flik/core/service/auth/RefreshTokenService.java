package yunrry.flik.core.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.exception.*;
import yunrry.flik.core.domain.model.AuthProvider;
import yunrry.flik.core.domain.model.OAuthUserInfo;
import yunrry.flik.core.domain.model.RefreshToken;
import yunrry.flik.core.domain.model.User;
import yunrry.flik.ports.in.command.CompleteOAuthSignupCommand;
import yunrry.flik.ports.in.command.LoginCommand;
import yunrry.flik.ports.in.command.OAuthLoginCommand;
import yunrry.flik.ports.in.command.RefreshTokenCommand;
import yunrry.flik.ports.in.usecase.*;
import yunrry.flik.ports.out.oauth.OAuth2Client;
import yunrry.flik.ports.out.repository.RefreshTokenRepository;
import yunrry.flik.ports.out.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

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

    @Service
    @RequiredArgsConstructor
    public static class LoginService implements LoginUseCase {

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

    @Service
    @RequiredArgsConstructor
    public static class LogoutService implements LogoutUseCase {

        private final RefreshTokenRepository refreshTokenRepository;
        private final JwtTokenProvider jwtTokenProvider;

        @Override
        public void logout(String refreshToken) {
            if (jwtTokenProvider.validateToken(refreshToken)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
                refreshTokenRepository.deleteByUserId(userId);
            }
        }

        @Override
        public void LogoutById(Long userId) {
            refreshTokenRepository.deleteByUserId(userId);
        }
    }


    @Service
    @RequiredArgsConstructor
    public static class OAuth2Service {

        private final OAuth2Client googleOAuth2Client;
        private final OAuth2Client kakaoOAuth2Client;

        public String getAuthorizationUrl(AuthProvider provider, String state) {
            return getOAuth2Client(provider).getAuthorizationUrl(state);
        }

        public OAuthUserInfo getUserInfo(AuthProvider provider, String code, String state) {
            return getOAuth2Client(provider).getUserInfo(code, state);
        }

        private OAuth2Client getOAuth2Client(AuthProvider provider) {
            System.out.println("Provider: " + provider);
            return switch (provider) {
                case GOOGLE -> googleOAuth2Client;
                case KAKAO -> kakaoOAuth2Client;
                default -> throw new IllegalArgumentException("지원하지 않는 OAuth 제공자입니다: " + provider);
            };
        }
    }

    @Service
    @RequiredArgsConstructor
    public static class OAuthSignupService implements OAuthSignupUseCase {

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
}