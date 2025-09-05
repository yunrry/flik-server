package yunrry.flik.ports.in.command;

import lombok.Builder;
import lombok.Getter;
import yunrry.flik.core.domain.model.AuthProvider;

@Getter
@Builder
public class CompleteOAuthSignupCommand {
    private final AuthProvider provider;
    private final String code;
    private final String state;
    private final String nickname;

    public void validate() {
        if (provider == null) {
            throw new IllegalArgumentException("인증 제공자는 필수입니다");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("인증 코드는 필수입니다");
        }
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수입니다");
        }
        if (nickname.length() > 20) {
            throw new IllegalArgumentException("닉네임은 20자를 초과할 수 없습니다");
        }
    }
}