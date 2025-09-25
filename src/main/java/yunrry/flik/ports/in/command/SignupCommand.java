package yunrry.flik.ports.in.usecase;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupCommand {
    private final String email;
    private final String password;
    private final String nickname;
    private final String profileImageUrl;
    private final boolean isGuest;

    public void validate() {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다");
        }
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수입니다");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다");
        }
        if (nickname.length() > 20) {
            throw new IllegalArgumentException("닉네임은 20자를 초과할 수 없습니다");
        }
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }
}