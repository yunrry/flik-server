package yunrry.flik.adapters.out.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import yunrry.flik.config.OAuth2Config;
import yunrry.flik.core.domain.model.AuthProvider;
import yunrry.flik.core.domain.model.OAuthUserInfo;
import yunrry.flik.ports.out.oauth.OAuth2Client;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class GoogleOAuth2Client implements OAuth2Client {

    private final OAuth2Config oAuth2Config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public String getAuthorizationUrl(String state) {
        OAuth2Config.OAuth2Provider google = oAuth2Config.getProviders().get("google");

        return google.getAuthorizationUri() +
                "?client_id=" + google.getClientId() +
                "&redirect_uri=" + URLEncoder.encode(google.getRedirectUri(), StandardCharsets.UTF_8) +
                "&scope=" + URLEncoder.encode(google.getScope(), StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&state=" + state;
    }

    @Override
    public OAuthUserInfo getUserInfo(String code, String state) {
        String accessToken = getAccessToken(code);
        return fetchUserInfo(accessToken);
    }

    private String getAccessToken(String code) {
        OAuth2Config.OAuth2Provider google = oAuth2Config.getProviders().get("google");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", google.getClientId());
        params.add("client_secret", google.getClientSecret());
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", google.getRedirectUri());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    google.getTokenUri(),
                    HttpMethod.POST,
                    request,
                    String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            throw new OAuth2AuthenticationException();
        }
    }

    private OAuthUserInfo fetchUserInfo(String accessToken) {
        OAuth2Config.OAuth2Provider google = oAuth2Config.getProviders().get("google");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    google.getUserInfoUri(),
                    HttpMethod.GET,
                    request,
                    String.class
            );

            JsonNode userInfo = objectMapper.readTree(response.getBody());

            return OAuthUserInfo.builder()
                    .providerId(userInfo.get("id").asText())
                    .email(userInfo.get("email").asText())
                    .nickname(userInfo.get("name").asText())
                    .profileImageUrl(userInfo.get("picture").asText())
                    .provider(AuthProvider.GOOGLE)
                    .build();
        } catch (Exception e) {
            throw new OAuth2AuthenticationException();
        }
    }
}