package yunrry.flik.adapters.out.persistence.mysql.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yunrry.flik.adapters.out.persistence.mysql.entity.converter.LongListConverter;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.core.domain.model.PostMetadata;
import yunrry.flik.core.domain.model.PostType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_nickname", nullable = false)
    private String userNickname;

    @Column(name = "user_profile_image_url")
    private String userProfileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    @Column(name = "visit_count", nullable = false)
    private Integer visitCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Metadata fields
    @Column(name = "region_code", length = 500)
    private String regionCode;

    @Column(name = "rating", precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(name = "spot_ids")
    @Convert(converter = LongListConverter.class)
    private List<Long> spotIds;

    @Column(name = "all_spot_ids")
    @Convert(converter = LongListConverter.class)
    private List<Long> allSpotIds;

    @Column(name = "course_id")
    private Long courseId;

    public Post toDomain() {
        return Post.builder()
                .id(this.id)
                .userId(this.userId)
                .userNickname(this.userNickname)
                .userProfileImageUrl(this.userProfileImageUrl)
                .type(this.type)
                .title(this.title)
                .content(this.content)
                .imageUrls(parseImageUrls(this.imageUrls))
                .metadata(createMetadata())
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .visitCount(this.visitCount)
                .spotIds(this.spotIds)
                .courseId(this.courseId)
                .build();
    }

    public static PostEntity fromDomain(Post post) {
        return PostEntity.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .userNickname(post.getUserNickname())
                .userProfileImageUrl(post.getUserProfileImageUrl())
                .type(post.getType())
                .title(post.getTitle())
                .content(post.getContent())
                .imageUrls(joinImageUrls(post.getImageUrls()))
                .visitCount(post.getVisitCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .regionCode(post.getMetadata() != null ? post.getMetadata().getRegionCode() : null)
                .allSpotIds(post.getMetadata().getSpotIds()!= null ? post.getMetadata().getSpotIds() : null)
                .spotIds(post.getSpotIds() != null ? post.getSpotIds() : null)
                .courseId(post.getCourseId() != null ? post.getCourseId() : null)
                .build();
    }

    private List<String> parseImageUrls(String imageUrls) {
        if (imageUrls == null || imageUrls.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.asList(imageUrls.split(","));
    }

    private static String joinImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return null;
        }
        return String.join(",", imageUrls);
    }

    private PostMetadata createMetadata() {
        return PostMetadata.builder()
                .regionCode(this.regionCode)
                .rating(this.rating)
                .spotIds(this.allSpotIds)
                .courseId(this.courseId)
                .build();
    }
}