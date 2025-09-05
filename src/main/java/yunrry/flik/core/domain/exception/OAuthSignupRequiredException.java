package yunrry.flik.core.domain.exception;

import yunrry.flik.core.domain.exception.common.FlikException;
import yunrry.flik.core.domain.model.OAuthUserInfo;

public class OAuthSignupRequiredException extends FlikException {

    private final OAuthUserInfo oAuthUserInfo;

    public OAuthSignupRequiredException(OAuthUserInfo oAuthUserInfo) {
        super(UserErrorCode.OAUTH_SIGNUP_REQUIRED, "AUTH");
        this.oAuthUserInfo = oAuthUserInfo;
    }

    public OAuthUserInfo getOAuthUserInfo() {
        return oAuthUserInfo;
    }
}