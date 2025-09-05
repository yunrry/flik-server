package yunrry.flik.ports.out.oauth;

import yunrry.flik.core.domain.model.OAuthUserInfo;

public interface OAuth2Client {
    String getAuthorizationUrl(String state);
    OAuthUserInfo getUserInfo(String code, String state);
}