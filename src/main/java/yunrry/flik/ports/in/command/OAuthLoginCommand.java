package yunrry.flik.ports.in.command;

import lombok.Builder;
import lombok.Getter;
import yunrry.flik.core.domain.model.AuthProvider;

@Getter
@Builder
public class OAuthLoginCommand {
    private final AuthProvider provider;
    private final String code;
    private final String state;

    public void validate() {
        if (provider == null) {
            throw new IllegalArgumentException("인증 제공자는 필수입니다");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("인증 코드는 필수입니다");
        }
        if (provider == AuthProvider.EMAIL) {
            throw new IllegalArgumentException("OAuth 로그인에는 소셜 제공자만 가능합니다");
        }
    }
}