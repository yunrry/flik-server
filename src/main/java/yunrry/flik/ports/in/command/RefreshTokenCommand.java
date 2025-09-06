package yunrry.flik.ports.in.command;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RefreshTokenCommand {
    private final String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void validate() {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("리프레시 토큰은 필수입니다");
        }
    }
}