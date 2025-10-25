package yunrry.flik.adapters.in.dto.auth;

import yunrry.flik.ports.in.usecase.AuthTokens;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UserInfo user
) {
    public static LoginResponse from(AuthTokens tokens) {
        return new LoginResponse(
                tokens.getAccessToken(),
                tokens.getRefreshToken(),
                UserInfo.from(tokens.getUser())
        );
    }


}