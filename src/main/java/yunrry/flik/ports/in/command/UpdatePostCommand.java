package yunrry.flik.ports.in.command;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UpdatePostCommand {
    private final Long postId;
    private final Long userId;
    private final String title;
    private final String content;
    private final List<String> imageUrls;

    public void validate() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수입니다");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 필수입니다");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("제목은 100자를 초과할 수 없습니다");
        }
        if (content.length() > 5000) {
            throw new IllegalArgumentException("내용은 5000자를 초과할 수 없습니다");
        }
    }
}