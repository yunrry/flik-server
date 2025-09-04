package yunrry.flik.adapters.in.dto.post;

import yunrry.flik.core.domain.model.Post;

public record CreatePostResponse(
        Long id,
        String message
) {
    public static CreatePostResponse from(Post post) {
        return new CreatePostResponse(
                post.getId(),
                "게시물이 성공적으로 생성되었습니다."
        );
    }
}