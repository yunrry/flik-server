package yunrry.flik.core.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@Builder
public class User {
    private final Long id;
    private final String email;
    private final String password;
    private final String nickname;
    private final String profileImageUrl;
    private final AuthProvider authProvider;
    private final String providerId;
    private final boolean isActive;
    private final LocalDateTime createdAt;
    private final LocalDateTime lastLoginAt;

    public User updateLastLogin() {  // void -> User 변경
        return User.builder()
                .id(this.id)
                .email(this.email)
                .password(this.password)
                .nickname(this.nickname)
                .profileImageUrl(this.profileImageUrl)
                .authProvider(this.authProvider)
                .providerId(this.providerId)
                .isActive(this.isActive)
                .createdAt(this.createdAt)
                .lastLoginAt(LocalDateTime.now())
                .build();
    }

    public User updateProfile(String nickname, String profileImageUrl) {
        return User.builder()
                .id(this.id)
                .email(this.email)
                .password(this.password)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .authProvider(this.authProvider)
                .providerId(this.providerId)
                .isActive(this.isActive)
                .createdAt(this.createdAt)
                .lastLoginAt(this.lastLoginAt)
                .build();
    }

    public boolean isOAuthUser() {
        return authProvider != AuthProvider.EMAIL;
    }

    public boolean canLogin() {
        return isActive;
    }

    public User deactivate() {
        return User.builder()
                .id(this.id)
                .email(this.email)
                .password(this.password)
                .nickname(this.nickname)
                .profileImageUrl(this.profileImageUrl)
                .authProvider(this.authProvider)
                .providerId(this.providerId)
                .isActive(false)
                .createdAt(this.createdAt)
                .lastLoginAt(this.lastLoginAt)
                .build();
    }
}