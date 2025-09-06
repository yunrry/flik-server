package yunrry.flik.ports.in.command;

import lombok.Builder;
import lombok.Getter;
import yunrry.flik.core.domain.model.AuthProvider;
import yunrry.flik.core.domain.model.OAuthUserInfo;

@Getter
@Builder
public class CompleteOAuthSignupCommand {
    private final OAuthUserInfo oAuthUserInfo;
    private final String nickname;

    public void validate() {

        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수입니다");
        }
        if (nickname.length() > 20) {
            throw new IllegalArgumentException("닉네임은 20자를 초과할 수 없습니다");
        }
    }
}