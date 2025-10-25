package yunrry.flik.adapters.in.dto.auth;

import yunrry.flik.ports.in.usecase.AuthTokens;

public record SignupResponse(
        String accessToken,
        String refreshToken,
        UserInfo user
) {
    public static SignupResponse from(AuthTokens tokens) {
        return new SignupResponse(
                tokens.getAccessToken(),
                tokens.getRefreshToken(),
                UserInfo.from(tokens.getUser())
        );
    }
}