package yunrry.flik.adapters.in.dto.auth;

import yunrry.flik.core.domain.model.OAuthUserInfo;

public record OAuthSignupResponse(
        String email,
        String defaultNickname,
        String profileImageUrl,
        String provider,
        String providerId,
        boolean signupRequired
) {
    public static OAuthSignupResponse from(OAuthUserInfo oAuthUserInfo) {
        return new OAuthSignupResponse(
                oAuthUserInfo.getEmail(),
                oAuthUserInfo.getNickname(),
                oAuthUserInfo.getProfileImageUrl(),
                oAuthUserInfo.getProvider().getCode(),
                oAuthUserInfo.getProviderId(),
                true
        );
    }
}