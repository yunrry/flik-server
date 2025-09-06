package yunrry.flik.adapters.in.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OAuth 회원가입 완료 요청")
public record CompleteOAuthSignupRequest(
        @Schema(description = "OAuth 제공자", example = "google")
        String provider,

        @Schema(description = "인증 코드")
        String tempKey,

        @Schema(description = "사용자 설정 닉네임", example = "새로운닉네임")
        String nickname
) {
}