package yunrry.flik.adapters.in.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 갱신 요청")
public record RefreshTokenRequest(
        @Schema(description = "리프레시 토큰")
        String refreshToken
) {
}