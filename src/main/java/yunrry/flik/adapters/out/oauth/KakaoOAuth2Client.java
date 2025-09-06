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

            // 응답 로그 추가 (디버깅용)
            System.out.println("Kakao API Response: " + response.getBody());

            JsonNode kakaoAccount = userInfo.get("kakao_account");

            // null 체크와 함께 안전하게 값 추출
            String providerId = userInfo.has("id") ? userInfo.get("id").asText() : null;
            String email = (kakaoAccount != null && kakaoAccount.has("email")) ?
                    kakaoAccount.get("email").asText() : providerId+"@kakao.com"; // 이메일이 없으면 대체 이메일 생성

            // 필수 필드 검증 (providerId만)
            if (providerId == null) {
                throw new OAuth2AuthenticationException("OAUTH-카카오 사용자 ID를 가져올 수 없습니다");
            }

            return OAuthUserInfo.builder()
                    .providerId(providerId)
                    .email(email) // null일 수 있음
                    .nickname(null) // 사용자가 나중에 설정
                    .profileImageUrl(null) // 사용하지 않음
                    .provider(AuthProvider.KAKAO)
                    .build();

        } catch (OAuth2AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error parsing Kakao response: " + e.getMessage());
            e.printStackTrace();
            throw new OAuth2AuthenticationException("OAUTH-소셜 로그인 인증에 실패했습니다");
        }
    }
}