package yunrry.flik.core.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("사용자 도메인 모델 테스트")
class UserTest {

    @Test
    @DisplayName("사용자 생성 시 필수 정보가 올바르게 설정된다")
    void shouldCreateUserWithRequiredFields() {
        // given & when
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("테스트사용자")
                .authProvider(AuthProvider.EMAIL)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        // then
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getNickname()).isEqualTo("테스트사용자");
        assertThat(user.getAuthProvider()).isEqualTo(AuthProvider.EMAIL);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("마지막 로그인 시간 업데이트가 정상적으로 동작한다")
    void shouldUpdateLastLoginTime() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .lastLoginAt(LocalDateTime.now().minusDays(1))
                .build();

        LocalDateTime beforeUpdate = user.getLastLoginAt();

        // when
        User updatedUser =  user.updateLastLogin();

        // then
        assertThat(updatedUser.getLastLoginAt()).isAfter(beforeUpdate);
    }

    @Test
    @DisplayName("프로필 수정이 정상적으로 동작한다")
    void shouldUpdateProfile() {
        // given
        User originalUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("원본닉네임")
                .profileImageUrl("original.jpg")
                .authProvider(AuthProvider.EMAIL)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        // when
        User updatedUser = originalUser.updateProfile("새닉네임", "new.jpg");

        // then
        assertThat(updatedUser.getNickname()).isEqualTo("새닉네임");
        assertThat(updatedUser.getProfileImageUrl()).isEqualTo("new.jpg");
        assertThat(updatedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(updatedUser.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("OAuth 사용자 여부를 정확히 판단한다")
    void shouldIdentifyOAuthUser() {
        // given
        User emailUser = User.builder().authProvider(AuthProvider.EMAIL).build();
        User googleUser = User.builder().authProvider(AuthProvider.GOOGLE).build();
        User kakaoUser = User.builder().authProvider(AuthProvider.KAKAO).build();

        // when & then
        assertThat(emailUser.isOAuthUser()).isFalse();
        assertThat(googleUser.isOAuthUser()).isTrue();
        assertThat(kakaoUser.isOAuthUser()).isTrue();
    }

    @Test
    @DisplayName("로그인 가능 여부를 정확히 판단한다")
    void shouldCheckLoginAvailability() {
        // given
        User activeUser = User.builder().isActive(true).build();
        User inactiveUser = User.builder().isActive(false).build();

        // when & then
        assertThat(activeUser.canLogin()).isTrue();
        assertThat(inactiveUser.canLogin()).isFalse();
    }

    @Test
    @DisplayName("사용자 비활성화가 정상적으로 동작한다")
    void shouldDeactivateUser() {
        // given
        User activeUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .isActive(true)
                .build();

        // when
        User deactivatedUser = activeUser.deactivate();

        // then
        assertThat(deactivatedUser.isActive()).isFalse();
        assertThat(deactivatedUser.getId()).isEqualTo(1L);
        assertThat(deactivatedUser.getEmail()).isEqualTo("test@example.com");
    }
}