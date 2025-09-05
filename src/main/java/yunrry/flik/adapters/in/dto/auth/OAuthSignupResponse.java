package yunrry.flik.adapters.in.dto.auth;

import yunrry.flik.core.domain.model.OAuthUserInfo;

public record OAuthSignupResponse(
        String email,
        String defaultNickname,
        String profileImageUrl,
        String provider,
        boolean signupRequired
) {
    public static OAuthSignupResponse from(OAuthUserInfo oAuthUserInfo) {
        return new OAuthSignupResponse(
                oAuthUserInfo.getEmail(),
                oAuthUserInfo.getNickname(),
                oAuthUserInfo.getProfileImageUrl(),
                oAuthUserInfo.getProvider().getCode(),
                true
        );
    }
}