package yunrry.flik.adapters.in.dto.post;

import yunrry.flik.core.domain.model.Post;

public record UpdatePostResponse(
        Long id,
        String message
) {
    public static UpdatePostResponse from(Post post) {
        return new UpdatePostResponse(
                post.getId(),
                "게시물이 성공적으로 수정되었습니다."
        );
    }
}