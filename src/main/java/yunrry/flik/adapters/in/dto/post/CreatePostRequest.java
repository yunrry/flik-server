package yunrry.flik.adapters.in.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "게시물 생성 요청")
public record CreatePostRequest(
        @Schema(description = "게시물 타입", example = "review", allowableValues = {"save", "review", "visit", "like", "share"})
        String type,

        @Schema(description = "제목", example = "성수동 맛집 리뷰")
        String title,

        @Schema(description = "내용", example = "마리오네에서 먹은 피자가 정말 맛있었습니다...")
        String content,

        @Schema(description = "이미지 URL 목록")
        List<String> imageUrl
) {
}