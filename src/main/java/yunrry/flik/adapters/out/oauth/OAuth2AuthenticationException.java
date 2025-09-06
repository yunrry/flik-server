package yunrry.flik.adapters.out.oauth;

import yunrry.flik.core.domain.exception.common.FlikException;
import yunrry.flik.core.domain.exception.UserErrorCode;

public class OAuth2AuthenticationException extends FlikException {

    public OAuth2AuthenticationException() {
        super(UserErrorCode.OAUTH_AUTHENTICATION_FAILED, "OAUTH");
    }

    public OAuth2AuthenticationException(String message) {
        super(UserErrorCode.OAUTH_AUTHENTICATION_FAILED, "OAUTH", message);
    }
}