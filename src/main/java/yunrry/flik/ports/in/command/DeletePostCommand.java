package yunrry.flik.ports.in.command;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeletePostCommand {
    private final Long postId;
    private final Long userId;

    public Long getPostId() {
        return postId;
    }

    public Long getUserId() {
        return userId;
    }
}