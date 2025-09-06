package yunrry.flik.adapters.in.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OAuth 콜백 요청")
public record OAuthCallbackRequest(
        @Schema(description = "OAuth 제공자", example = "google", allowableValues = {"google", "kakao"})
        String provider,

        @Schema(description = "인증 코드")
        String code,

        @Schema(description = "상태값")
        String state
) {
}