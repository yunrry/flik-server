package yunrry.flik.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yunrry.flik.core.domain.model.AuthProvider;
import yunrry.flik.core.domain.model.OAuthUserInfo;
import yunrry.flik.core.service.auth.RefreshTokenService;
import yunrry.flik.ports.out.oauth.OAuth2Client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

// OAuth2ServiceTest.java - 완전히 수정된 버전
@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2 서비스 테스트")
class OAuth2ServiceTest {

    @Mock
    private OAuth2Client googleOAuth2Client;

    @Mock
    private OAuth2Client kakaoOAuth2Client;

    @InjectMocks
    private RefreshTokenService.OAuth2Service oAuth2Service;

    @BeforeEach
    void setUp() {
        // 수동으로 OAuth2Service 생성하여 Mock 주입
        oAuth2Service = new RefreshTokenService.OAuth2Service(googleOAuth2Client, kakaoOAuth2Client);
    }


    @Test
    @DisplayName("Google 인증 URL 생성이 성공한다")
    void shouldGetGoogleAuthorizationUrl() {
        // given
        String state = "random_state";
        String expectedUrl = "https://accounts.google.com/oauth/authorize?client_id=test";

        given(googleOAuth2Client.getAuthorizationUrl(state)).willReturn(expectedUrl);

        // when
        String result = oAuth2Service.getAuthorizationUrl(AuthProvider.GOOGLE, state);

        // then
        assertThat(result).isEqualTo(expectedUrl);
        then(googleOAuth2Client).should().getAuthorizationUrl(state);
    }

    @Test
    @DisplayName("Kakao 인증 URL 생성이 성공한다")
    void shouldGetKakaoAuthorizationUrl() {
        // given
        String state = "random_state";
        String expectedUrl = "https://kauth.kakao.com/oauth/authorize?client_id=test";

        given(kakaoOAuth2Client.getAuthorizationUrl(state)).willReturn(expectedUrl);

        // when
        String result = oAuth2Service.getAuthorizationUrl(AuthProvider.KAKAO, state);

        // then
        assertThat(result).isEqualTo(expectedUrl);
        then(kakaoOAuth2Client).should().getAuthorizationUrl(state);
    }

    @Test
    @DisplayName("Google 사용자 정보 조회가 성공한다")
    void shouldGetGoogleUserInfo() {
        // given
        String code = "auth_code";
        String state = "random_state";

        OAuthUserInfo expectedUserInfo = OAuthUserInfo.builder()
                .providerId("google123")
                .email("user@gmail.com")
                .nickname("구글사용자")
                .profileImageUrl("https://google.com/profile.jpg")
                .provider(AuthProvider.GOOGLE)
                .build();

        given(googleOAuth2Client.getUserInfo(code, state)).willReturn(expectedUserInfo);

        // when
        OAuthUserInfo result = oAuth2Service.getUserInfo(AuthProvider.GOOGLE, code, state);

        // then
        assertThat(result.getEmail()).isEqualTo("user@gmail.com");
        assertThat(result.getNickname()).isEqualTo("구글사용자");
        assertThat(result.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        then(googleOAuth2Client).should().getUserInfo(code, state);
    }

    @Test
    @DisplayName("Kakao 사용자 정보 조회가 성공한다")
    void shouldGetKakaoUserInfo() {
        // given
        String code = "auth_code";
        String state = "random_state";

        OAuthUserInfo expectedUserInfo = OAuthUserInfo.builder()
                .providerId("kakao123")
                .email("user@kakao.com")
                .nickname("카카오사용자")
                .profileImageUrl("https://kakao.com/profile.jpg")
                .provider(AuthProvider.KAKAO)
                .build();

        given(kakaoOAuth2Client.getUserInfo(code, state)).willReturn(expectedUserInfo);

        // when
        OAuthUserInfo result = oAuth2Service.getUserInfo(AuthProvider.KAKAO, code, state);

        // then
        assertThat(result.getEmail()).isEqualTo("user@kakao.com");
        assertThat(result.getNickname()).isEqualTo("카카오사용자");
        assertThat(result.getProvider()).isEqualTo(AuthProvider.KAKAO);
        then(kakaoOAuth2Client).should().getUserInfo(code, state);
    }

    @Test
    @DisplayName("지원하지 않는 OAuth 제공자로 요청 시 예외가 발생한다")
    void shouldThrowExceptionForUnsupportedProvider() {
        // given
        String state = "random_state";

        // when & then
        assertThatThrownBy(() -> oAuth2Service.getAuthorizationUrl(AuthProvider.EMAIL, state))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지원하지 않는 OAuth 제공자입니다: EMAIL");
    }
}