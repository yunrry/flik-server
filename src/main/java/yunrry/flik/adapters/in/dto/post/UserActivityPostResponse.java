package yunrry.flik.adapters.in.dto.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import yunrry.flik.core.domain.model.Post;

import java.time.LocalDateTime;

public record UserActivityPostResponse(
        Long id,
        Long userId,
        String type,
        Integer visitCount,
        String title,
        String description,
        String imageUrl,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
) {
    public static UserActivityPostResponse from(Post post) {
        String imageUrl = post.getImageUrls() != null && !post.getImageUrls().isEmpty()
                ? post.getImageUrls().get(0) : null;

        String description = post.getContent().length() > 100
                ? post.getContent().substring(0, 100) + "..."
                : post.getContent();

        return new UserActivityPostResponse(
                post.getId(),
                post.getUserId(),
                post.getType().getCode(),
                post.getVisitCount(),
                post.getTitle(),
                description,
                imageUrl,
                post.getCreatedAt()
        );
    }
}