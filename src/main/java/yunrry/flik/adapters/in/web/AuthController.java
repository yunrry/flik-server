package yunrry.flik.adapters.in.web;

import com.nimbusds.openid.connect.sdk.LogoutRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yunrry.flik.adapters.in.dto.Response;
import yunrry.flik.adapters.in.dto.auth.*;
import yunrry.flik.core.domain.exception.OAuthSignupRequiredException;
import yunrry.flik.core.domain.model.AuthProvider;
import yunrry.flik.core.domain.model.User;
import yunrry.flik.core.service.OAuth2Service;
import yunrry.flik.ports.in.command.CompleteOAuthSignupCommand;
import yunrry.flik.ports.in.command.LoginCommand;
import yunrry.flik.ports.in.command.OAuthLoginCommand;
import yunrry.flik.ports.in.command.RefreshTokenCommand;
import yunrry.flik.ports.in.usecase.*;
import yunrry.flik.ports.in.usecase.SignupCommand;

@Tag(name = "Authentication", description = "인증 API")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final SignupUseCase signupUseCase;
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

    @Operation(summary = "OAuth 회원가입 완료", description = "OAuth 첫 로그인 시 닉네임을 설정하여 회원가입을 완료합니다.")
    @PostMapping("/oauth/signup")
    public ResponseEntity<Response<LoginResponse>> completeOAuthSignup(@RequestBody CompleteOAuthSignupRequest request) {
        CompleteOAuthSignupCommand command = CompleteOAuthSignupCommand.builder()
                .provider(AuthProvider.fromCode(request.provider()))
                .code(request.code())
                .state(request.state())
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
        // 리프레시 토큰 삭제 로직 추가 필요
        return ResponseEntity.ok(Response.success(null));
    }
}