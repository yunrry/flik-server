package yunrry.flik.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import yunrry.flik.core.domain.exception.InvalidTokenException;
import yunrry.flik.core.domain.model.RefreshToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JWT 토큰 제공자 테스트")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", "test-secret-key-that-is-long-enough-for-hmac-sha256");
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiry", 3600L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiry", 604800L);
    }

    @Test
    @DisplayName("액세스 토큰 생성이 성공한다")
    void shouldCreateAccessToken() {
        // given
        Long userId = 123L;

        // when
        String token = jwtTokenProvider.createAccessToken(userId);

        // then
        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3); // JWT 형식 확인
    }

    @Test
    @DisplayName("리프레시 토큰 생성이 성공한다")
    void shouldCreateRefreshToken() {
        // given
        Long userId = 123L;

        // when
        RefreshToken refreshToken = jwtTokenProvider.createRefreshToken(userId);

        // then
        assertThat(refreshToken.getToken()).isNotNull();
        assertThat(refreshToken.getUserId()).isEqualTo(userId);
        assertThat(refreshToken.getExpiryTime()).isNotNull();
        assertThat(refreshToken.isValid()).isTrue();
    }

    @Test
    @DisplayName("유효한 토큰에서 사용자 ID 추출이 성공한다")
    void shouldGetUserIdFromValidToken() {
        // given
        Long userId = 123L;
        String token = jwtTokenProvider.createAccessToken(userId);

        // when
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        // then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("유효한 토큰 검증이 성공한다")
    void shouldValidateValidToken() {
        // given
        Long userId = 123L;
        String token = jwtTokenProvider.createAccessToken(userId);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증이 실패한다")
    void shouldFailToValidateInvalidToken() {
        // given
        String invalidToken = "invalid.token.here";

        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("유효하지 않은 토큰에서 사용자 ID 추출 시 예외가 발생한다")
    void shouldThrowExceptionForInvalidToken() {
        // given
        String invalidToken = "invalid.token.here";

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromToken(invalidToken))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("빈 토큰 검증이 실패한다")
    void shouldFailToValidateEmptyToken() {
        // when
        boolean isValid = jwtTokenProvider.validateToken("");

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("null 토큰 검증이 실패한다")
    void shouldFailToValidateNullToken() {
        // when
        boolean isValid = jwtTokenProvider.validateToken(null);

        // then
        assertThat(isValid).isFalse();
    }
}