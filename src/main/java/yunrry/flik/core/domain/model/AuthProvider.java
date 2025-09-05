package yunrry.flik.core.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthProvider {
    EMAIL("email", "이메일"),
    GOOGLE("google", "구글"),
    KAKAO("kakao", "카카오");

    private final String code;
    private final String description;

    public static AuthProvider fromCode(String code) {
        for (AuthProvider provider : values()) {
            if (provider.code.equals(code)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 인증 제공자입니다: " + code);
    }
}