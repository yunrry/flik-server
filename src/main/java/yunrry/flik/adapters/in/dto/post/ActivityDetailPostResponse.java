package yunrry.flik.adapters.in.dto.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.core.domain.model.PostMetadata;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.core.domain.model.plan.TravelCourse;

import java.time.LocalDateTime;
import java.util.List;

public record ActivityDetailPostResponse(
        String id,
        Long userId,
        String userName,
        String userProfileImageUrl,
        String type,
        Integer visitCount,
        String title,
        String content,
        List<String> imageUrls,
        String regionCode,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        PostCourseMetaResponse courseMetaResponse,
        List<PostSpotMetaResponse> spotMetaResponses
) {
    public static ActivityDetailPostResponse from(Post post, TravelCourse travelCourse, List<Spot> spots) {
        return new ActivityDetailPostResponse(
                post.getId().toString(),
                post.getUserId(),
                post.getUserNickname(),
                post.getUserProfileImageUrl(),
                post.getType().getCode(),
                post.getVisitCount(),
                post.getTitle(),
                post.getContent(),
                post.getImageUrls(),
                post.getMetadata() != null ? post.getMetadata().getRegionCode() : null,
                post.getCreatedAt(),
                travelCourse !=null? PostCourseMetaResponse.from(travelCourse) : null,
                spots != null? spots.stream().map(PostSpotMetaResponse::from).toList() : null
        );
    }
}