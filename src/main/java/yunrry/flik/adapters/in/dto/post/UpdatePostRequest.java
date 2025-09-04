package yunrry.flik.adapters.in.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "게시물 수정 요청")
public record UpdatePostRequest(
        @Schema(description = "제목", example = "수정된 제목")
        String title,

        @Schema(description = "내용", example = "수정된 내용")
        String content,

        @Schema(description = "이미지 URL 목록")
        List<String> imageUrl
) {
}