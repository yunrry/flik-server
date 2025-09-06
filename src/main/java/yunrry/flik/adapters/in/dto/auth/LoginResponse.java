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

    public record UserInfo(
            Long id,
            String email,
            String nickname,
            String profileImageUrl,
            String provider
    ) {
        public static UserInfo from(yunrry.flik.core.domain.model.User user) {
            return new UserInfo(
                    user.getId(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getProfileImageUrl(),
                    user.getAuthProvider().getCode()
            );
        }
    }
}