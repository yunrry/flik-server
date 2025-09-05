package yunrry.flik.adapters.in.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그아웃 요청")
public record LogoutRequest(
        @Schema(description = "리프레시 토큰")
        String refreshToken
) {
}