package yunrry.flik.adapters.in.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yunrry.flik.core.domain.model.AuthProvider;
import yunrry.flik.core.domain.model.OAuthUserInfo;

import java.io.Serializable;

// Redis 저장용 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthUserInfoCache implements Serializable {
    private static final long serialVersionUID = 1L;

    private String providerId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String provider; // AuthProvider를 String으로 저장

    public static OAuthUserInfoCache from(OAuthUserInfo userInfo) {
        return new OAuthUserInfoCache(
                userInfo.getProviderId(),
                userInfo.getEmail(),
                userInfo.getNickname(),
                userInfo.getProfileImageUrl(),
                userInfo.getProvider().name()
        );
    }

    public OAuthUserInfo toOAuthUserInfo() {
        return OAuthUserInfo.builder()
                .providerId(this.providerId)
                .email(this.email)
                .nickname(this.nickname)
                .profileImageUrl(this.profileImageUrl)
                .provider(AuthProvider.valueOf(this.provider))
                .build();
    }
}