package yunrry.flik.adapters.out.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import yunrry.flik.config.OAuth2Config;
import yunrry.flik.core.domain.model.AuthProvider;
import yunrry.flik.core.domain.model.OAuthUserInfo;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Google OAuth2 클라이언트 테스트")
class GoogleOAuth2ClientTest {

    @Mock
    private OAuth2Config oAuth2Config;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private GoogleOAuth2Client googleOAuth2Client;

    @BeforeEach
    void setUp() {
        googleOAuth2Client = new GoogleOAuth2Client(oAuth2Config, restTemplate, objectMapper);

        OAuth2Config.OAuth2Provider googleProvider = new OAuth2Config.OAuth2Provider();
        googleProvider.setClientId("test-client-id");
        googleProvider.setClientSecret("test-client-secret");
        googleProvider.setRedirectUri("http://localhost:8080/callback");
        googleProvider.setAuthorizationUri("https://accounts.google.com/o/oauth2/v2/auth");
        googleProvider.setTokenUri("https://oauth2.googleapis.com/token");
        googleProvider.setUserInfoUri("https://www.googleapis.com/oauth2/v2/userinfo");
        googleProvider.setScope("openid email profile");

        given(oAuth2Config.getProviders()).willReturn(Map.of("google", googleProvider));
    }

    @Test
    @DisplayName("Google 인증 URL 생성이 성공한다")
    void shouldGenerateAuthorizationUrl() {
        // given
        String state = "random_state";

        // when
        String authUrl = googleOAuth2Client.getAuthorizationUrl(state);

        // then
        assertThat(authUrl).contains("https://accounts.google.com/o/oauth2/v2/auth");
        assertThat(authUrl).contains("client_id=test-client-id");
        assertThat(authUrl).contains("state=random_state");
        assertThat(authUrl).contains("response_type=code");
    }

    @Test
    @DisplayName("액세스 토큰 획득 실패 시 예외가 발생한다")
    void shouldThrowExceptionWhenTokenRequestFails() {
        // given
        String code = "auth_code";
        String state = "random_state";

        given(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .willThrow(new RuntimeException("Token request failed"));

        // when & then
        assertThatThrownBy(() -> googleOAuth2Client.getUserInfo(code, state))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }
}