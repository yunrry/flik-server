package yunrry.flik.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.model.AuthProvider;
import yunrry.flik.core.domain.model.OAuthUserInfo;
import yunrry.flik.ports.out.oauth.OAuth2Client;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final OAuth2Client googleOAuth2Client;
    private final OAuth2Client kakaoOAuth2Client;

    public String getAuthorizationUrl(AuthProvider provider, String state) {
        return getOAuth2Client(provider).getAuthorizationUrl(state);
    }

    public OAuthUserInfo getUserInfo(AuthProvider provider, String code, String state) {
        return getOAuth2Client(provider).getUserInfo(code, state);
    }

    private OAuth2Client getOAuth2Client(AuthProvider provider) {
        System.out.println("Provider: " + provider);
        return switch (provider) {
            case GOOGLE -> googleOAuth2Client;
            case KAKAO -> kakaoOAuth2Client;
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth 제공자입니다: " + provider);
        };
    }
}