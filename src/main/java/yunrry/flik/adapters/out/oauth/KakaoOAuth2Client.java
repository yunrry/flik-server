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
public class KakaoOAuth2Client implements OAuth2Client {

    private final OAuth2Config oAuth2Config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public String getAuthorizationUrl(String state) {
        OAuth2Config.OAuth2Provider kakao = oAuth2Config.getProviders().get("kakao");

        return kakao.getAuthorizationUri() +
                "?client_id=" + kakao.getClientId() +
                "&redirect_uri=" + URLEncoder.encode(kakao.getRedirectUri(), StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&state=" + state;
    }

    @Override
    public OAuthUserInfo getUserInfo(String code, String state) {
        String accessToken = getAccessToken(code);
        return fetchUserInfo(accessToken);
    }

    private String getAccessToken(String code) {
        OAuth2Config.OAuth2Provider kakao = oAuth2Config.getProviders().get("kakao");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakao.getClientId());
        params.add("client_secret", kakao.getClientSecret());
        params.add("redirect_uri", kakao.getRedirectUri());
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    kakao.getTokenUri(),
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
        OAuth2Config.OAuth2Provider kakao = oAuth2Config.getProviders().get("kakao");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    kakao.getUserInfoUri(),
                    HttpMethod.GET,
                    request,
                    String.class
            );

            JsonNode userInfo = objectMapper.readTree(response.getBody());
            JsonNode kakaoAccount = userInfo.get("kakao_account");
            JsonNode profile = kakaoAccount.get("profile");

            return OAuthUserInfo.builder()
                    .providerId(userInfo.get("id").asText())
                    .email(kakaoAccount.get("email").asText())
                    .nickname(profile.get("nickname").asText())
                    .profileImageUrl(profile.get("profile_image_url").asText())
                    .provider(AuthProvider.KAKAO)
                    .build();
        } catch (Exception e) {
            throw new OAuth2AuthenticationException();
        }
    }
}