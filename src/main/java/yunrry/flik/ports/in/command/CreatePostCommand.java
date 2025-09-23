package yunrry.flik.ports.in.command;

import lombok.Builder;
import lombok.Getter;
import yunrry.flik.adapters.in.dto.post.PostCourseMetaResponse;
import yunrry.flik.core.domain.model.PostType;

import java.util.List;

@Getter
@Builder
public class CreatePostCommand {
    private final Long userId;
    private final PostType type;
    private final String title;
    private final String content;
    private final List<String> imageUrls;
    private final String regionCode;
    private final List<Long> spotIds;
    private final List<Long> relatedSpotIds;
    private final Long courseId;

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