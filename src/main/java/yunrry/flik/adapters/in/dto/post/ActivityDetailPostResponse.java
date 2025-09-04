package yunrry.flik.adapters.in.dto.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import yunrry.flik.core.domain.model.Post;

import java.time.LocalDateTime;
import java.util.List;

public record ActivityDetailPostResponse(
        String id,
        Long userId,
        String type,
        Integer visitCount,
        String title,
        String content,
        List<String> imageUrl,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        PostMetadataResponse metadata
) {
    public static ActivityDetailPostResponse from(Post post) {
        return new ActivityDetailPostResponse(
                post.getId().toString(),
                post.getUserId(),
                post.getType().getCode(),
                post.getVisitCount(),
                post.getTitle(),
                post.getContent(),
                post.getImageUrls(),
                post.getCreatedAt(),
                PostMetadataResponse.from(post.getMetadata())
        );
    }

    public record PostMetadataResponse(
            String restaurantName,
            String location,
            Double rating
    ) {
        public static PostMetadataResponse from(yunrry.flik.core.domain.model.PostMetadata metadata) {
            if (metadata == null) return null;

            return new PostMetadataResponse(
                    metadata.getRestaurantName(),
                    metadata.getLocation(),
                    metadata.getRating() != null ? metadata.getRating().doubleValue() : null
            );
        }
    }
}