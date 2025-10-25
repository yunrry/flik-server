package yunrry.flik.adapters.out.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import yunrry.flik.adapters.out.persistence.mysql.UserAdapter;
import yunrry.flik.adapters.out.persistence.mysql.entity.UserEntity;
import yunrry.flik.core.domain.model.AuthProvider;
import yunrry.flik.core.domain.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(UserAdapter.class)
@DisplayName("사용자 어댑터 테스트")
class UserAdapterTest {

    @Autowired
    private UserAdapter userAdapter;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("사용자 저장이 성공한다")
    void shouldSaveUser() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .nickname("테스트사용자")
                .password("encoded_password")
                .profileImageUrl(null)
                .isGuest(false)
                .authProvider(AuthProvider.EMAIL)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        // when
        User savedUser = userAdapter.save(user);

        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("테스트사용자");
        assertThat(savedUser.getAuthProvider()).isEqualTo(AuthProvider.EMAIL);
    }

    @Test
    @DisplayName("이메일로 사용자 조회가 성공한다")
    void shouldFindByEmail() {
        // given
        UserEntity entity = UserEntity.builder()
                .email("test@example.com")
                .password("password")
                .nickname("사용자")
                .authProvider(AuthProvider.EMAIL)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(entity);

        // when
        Optional<User> foundUser = userAdapter.findByEmail("test@example.com");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("OAuth 제공자와 ID로 사용자 조회가 성공한다")
    void shouldFindByProviderAndProviderId() {
        // given
        UserEntity entity = UserEntity.builder()
                .email("google@example.com")
                .nickname("구글사용자")
                .authProvider(AuthProvider.GOOGLE)
                .providerId("google123")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(entity);

        // when
        Optional<User> foundUser = userAdapter.findByProviderAndProviderId(
                AuthProvider.GOOGLE, "google123");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getProviderId()).isEqualTo("google123");
        assertThat(foundUser.get().getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
    }

    @Test
    @DisplayName("이메일 중복 체크가 정상적으로 동작한다")
    void shouldCheckEmailExists() {
        // given
        UserEntity entity = UserEntity.builder()
                .email("existing@example.com")
                .nickname("기존사용자")
                .authProvider(AuthProvider.EMAIL)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(entity);

        // when & then
        assertThat(userAdapter.existsByEmail("existing@example.com")).isTrue();
        assertThat(userAdapter.existsByEmail("nonexisting@example.com")).isFalse();
    }
}