package yunrry.flik.ports.in.query;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetPostQuery {
    private final Long postId;

    public Long getPostId() {
        return postId;
    }
}