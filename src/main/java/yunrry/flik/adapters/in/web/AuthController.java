package yunrry.flik.adapters.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yunrry.flik.adapters.in.dto.OAuthUserInfoCache;
import yunrry.flik.adapters.in.dto.Response;
import yunrry.flik.adapters.in.dto.auth.*;
import yunrry.flik.adapters.out.oauth.OAuth2AuthenticationException;
import yunrry.flik.core.domain.exception.OAuthSignupRequiredException;
import yunrry.flik.core.domain.model.AuthProvider;
import yunrry.flik.core.domain.model.OAuthUserInfo;
import yunrry.flik.core.domain.model.User;
import yunrry.flik.core.service.OAuth2Service;
import yunrry.flik.ports.in.command.CompleteOAuthSignupCommand;
import yunrry.flik.ports.in.command.LoginCommand;
import yunrry.flik.ports.in.command.OAuthLoginCommand;
import yunrry.flik.ports.in.command.RefreshTokenCommand;
import yunrry.flik.ports.in.usecase.*;
import yunrry.flik.ports.in.usecase.SignupCommand;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Tag(name = "Authentication", description = "인증 API")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RedisTemplate redisTemplate;

    @Value("${app.frontend.url:http://localhost:5713}")
    private String frontendUrl;

    private final LoginUseCase loginUseCase;
    private final SignupUseCase signupUseCase;
    private final LogoutUseCase logoutUseCase;
    private final OAuthSignupUseCase oAuthSignupUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final OAuth2Service oAuth2Service;

    @Operation(summary = "이메일 회원가입", description = "이메일과 비밀번호로 회원가입합니다.")
    @PostMapping("/signup")
    public ResponseEntity<Response<SignupResponse>> signup(@RequestBody SignupRequest request) {
        SignupCommand command = SignupCommand.builder()
                .email(request.email())
                .password(request.password())
                .nickname(request.nickname())
                .profileImageUrl(request.profileImageUrl())
                .build();

        User user = signupUseCase.signup(command);
        SignupResponse response = SignupResponse.from(user);

        return ResponseEntity.ok(Response.success(response));
    }

    @Operation(summary = "이메일 로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<Response<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginCommand command = LoginCommand.builder()
                .email(request.email())
                .password(request.password())
                .build();

        AuthTokens tokens = loginUseCase.login(command);
        LoginResponse response = LoginResponse.from(tokens);

        return ResponseEntity.ok(Response.success(response));
    }

    @Operation(summary = "Google 로그인 URL 조회", description = "Google OAuth 로그인 URL을 조회합니다.")
    @GetMapping("/oauth/google")
    public ResponseEntity<Response<OAuthUrlResponse>> getGoogleAuthUrl(@RequestParam String state) {
        String authUrl = oAuth2Service.getAuthorizationUrl(AuthProvider.GOOGLE, state);
        OAuthUrlResponse response = new OAuthUrlResponse(authUrl);

        return ResponseEntity.ok(Response.success(response));
    }

    @Operation(summary = "Kakao 로그인 URL 조회", description = "Kakao OAuth 로그인 URL을 조회합니다.")
    @GetMapping("/oauth/kakao")
    public ResponseEntity<Response<OAuthUrlResponse>> getKakaoAuthUrl(@RequestParam String state) {
        String authUrl = oAuth2Service.getAuthorizationUrl(AuthProvider.KAKAO, state);
        OAuthUrlResponse response = new OAuthUrlResponse(authUrl);

        return ResponseEntity.ok(Response.success(response));
    }

    @Operation(summary = "OAuth 콜백 처리", description = "OAuth 인증 코드를 처리합니다.")
    @PostMapping("/oauth/callback")
    public ResponseEntity<Response<?>> oauthCallback(@RequestBody OAuthCallbackRequest request) {
        try {
            OAuthLoginCommand command = OAuthLoginCommand.builder()
                    .provider(AuthProvider.fromCode(request.provider()))
                    .code(request.code())
                    .state(request.state())
                    .build();

            AuthTokens tokens = loginUseCase.oauthLogin(command);
            LoginResponse response = LoginResponse.from(tokens);

            return ResponseEntity.ok(Response.success(response));

        } catch (OAuthSignupRequiredException e) {
            // 첫 로그인인 경우 사용자 정보 반환
            OAuthSignupResponse response = OAuthSignupResponse.from(e.getOAuthUserInfo());
            return ResponseEntity.ok(Response.success(response));
        }
    }

    @GetMapping("/oauth/callback/{provider}")
    public void handleOAuthRedirect(
            @PathVariable String provider,
            @RequestParam String code,
            @RequestParam(required = false) String state,
            HttpServletResponse response) throws IOException {
            log.info("OAuth callback received - provider: {}, code: {}, state: {}", provider, code, state);
        try {
            // OAuth 로그인 처리
            log.info("Starting OAuth login process...");
            OAuthLoginCommand command = OAuthLoginCommand.builder()
                    .provider(AuthProvider.fromCode(provider))
                    .code(code)
                    .state(state)
                    .build();

            log.info("Calling loginUseCase.oauthLogin...");
            AuthTokens tokens = loginUseCase.oauthLogin(command);
            LoginResponse loginResponse = LoginResponse.from(tokens);

            // 로그인 성공 시 토큰과 함께 프론트엔드로 리다이렉트
            String successUrl = String.format(
                    "%s/auth/success?access_token=%s&refresh_token=%s",
                    frontendUrl,
                    URLEncoder.encode(loginResponse.accessToken(), StandardCharsets.UTF_8),
                    URLEncoder.encode(loginResponse.refreshToken(), StandardCharsets.UTF_8)
            );
            log.info("Redirecting to success URL: {}", successUrl);
            response.sendRedirect(successUrl);

        } catch (OAuthSignupRequiredException e) {
            log.info("OAuth signup required for new user");

            // Redis에 사용자 정보 임시 저장 (JSON으로 직렬화됨)
            String tempKey = "oauth_signup:" + UUID.randomUUID().toString();
            OAuthUserInfoCache cacheData = OAuthUserInfoCache.from(e.getOAuthUserInfo());

            // Redis에 10분간 저장
            redisTemplate.opsForValue().set(tempKey, cacheData, Duration.ofMinutes(10));

            String signupUrl = String.format(
                "%s/auth/signup?provider=%s&temp_key=%s",
                frontendUrl,
                provider,
                URLEncoder.encode(tempKey, StandardCharsets.UTF_8)
            );

            log.info("Redirecting to signup URL: {}", signupUrl);
            response.sendRedirect(signupUrl);

        } catch (Exception e) {
            log.error("OAuth callback failed", e);

            String errorUrl = String.format(
                    "%s/login?error=oauth_error&message=%s",
                    frontendUrl,
                    URLEncoder.encode("OAUTH-" + e.getMessage(), StandardCharsets.UTF_8)
            );

            log.info("Redirecting to error URL: {}", errorUrl);
            response.sendRedirect(errorUrl);
        }
    }

    @Operation(summary = "OAuth 회원가입 완료", description = "OAuth 첫 로그인 시 닉네임을 설정하여 회원가입을 완료합니다.")
    @PostMapping("/oauth/signup")
    public ResponseEntity<Response<LoginResponse>> completeOAuthSignup(@RequestBody CompleteOAuthSignupRequest request) {
        String tempKey = request.tempKey();

        // GenericJackson2JsonRedisSerializer가 자동으로 역직렬화
        OAuthUserInfoCache cachedData = (OAuthUserInfoCache) redisTemplate.opsForValue().get(tempKey);
        if (cachedData == null) {
            throw new OAuth2AuthenticationException("OAUTH-회원가입 세션이 만료되었습니다");
        }

        OAuthUserInfo userInfo = cachedData.toOAuthUserInfo();

        // Redis 데이터 삭제 (일회용)
        redisTemplate.delete(tempKey);

        // 회원가입 처리
        CompleteOAuthSignupCommand command = CompleteOAuthSignupCommand.builder()
                .oAuthUserInfo(userInfo)
                .nickname(request.nickname())
                .build();

        AuthTokens tokens = oAuthSignupUseCase.completeOAuthSignup(command);
        LoginResponse response = LoginResponse.from(tokens);

        return ResponseEntity.ok(Response.success(response));
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새로운 액세스 토큰을 발급받습니다.")
    @PostMapping("/refresh")
    public ResponseEntity<Response<RefreshTokenResponse>> refreshTokens(@RequestBody RefreshTokenRequest request) {
        RefreshTokenCommand command = new RefreshTokenCommand(request.refreshToken());
        AuthTokens tokens = refreshTokenUseCase.refreshTokens(command);
        RefreshTokenResponse response = RefreshTokenResponse.from(tokens);

        return ResponseEntity.ok(Response.success(response));
    }

    @Operation(summary = "로그아웃", description = "사용자를 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Response<Void>> logout(@RequestBody LogoutRequest request) {
        logoutUseCase.logout(request.refreshToken());
        return ResponseEntity.ok(Response.success(null));
    }
}