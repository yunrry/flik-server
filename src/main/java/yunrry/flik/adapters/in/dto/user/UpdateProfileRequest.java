// adapters/in/dto/user/UpdateProfileRequest.java
package yunrry.flik.adapters.in.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 수정 요청")
public record UpdateProfileRequest(
        @Schema(description = "닉네임", example = "새로운닉네임")
        String nickname,

        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl
) {
}