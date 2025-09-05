package yunrry.flik.adapters.in.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 요청")
public record SignupRequest(
        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "비밀번호", example = "password123")
        String password,

        @Schema(description = "닉네임", example = "사용자닉네임")
        String nickname,

        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl
) {
}