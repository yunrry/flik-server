package yunrry.flik.adapters.in.dto.auth;

import yunrry.flik.ports.in.usecase.AuthTokens;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken
) {
    public static RefreshTokenResponse from(AuthTokens tokens) {
        return new RefreshTokenResponse(
                tokens.getAccessToken(),
                tokens.getRefreshToken()
        );
    }
}