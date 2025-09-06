package yunrry.flik.ports.in.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginCommand {
    private final String email;
    private final String password;

    public void validate() {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다");
        }
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }
}