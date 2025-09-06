package yunrry.flik.core.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class OAuthUserInfo {
    private final String providerId;
    private final String email;
    private final String nickname;
    private final String profileImageUrl;
    private final AuthProvider provider;

    public User toUser() {
        return User.builder()
                .email(this.email)
                .nickname(this.nickname)
                .profileImageUrl(this.profileImageUrl)
                .authProvider(this.provider)
                .providerId(this.providerId)
                .isActive(true)
                .createdAt(java.time.LocalDateTime.now())
                .build();
    }
}