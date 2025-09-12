package yunrry.flik.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    @Column(name = "spot_name", length = 500)
    private String spotName;

    @Column(name = "location", length = 500)
    private String location;

    @Column(name = "rating", precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(name = "spot_id")
    private Long spotId;

    public Post toDomain() {
        return Post.builder()
                .id(this.id)
                .userId(this.userId)
                .type(this.type)
                .title(this.title)
                .content(this.content)
                .imageUrls(parseImageUrls(this.imageUrls))
                .metadata(createMetadata())
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .visitCount(this.visitCount)
                .build();
    }

    public static PostEntity fromDomain(Post post) {
        return PostEntity.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .type(post.getType())
                .title(post.getTitle())
                .content(post.getContent())
                .imageUrls(joinImageUrls(post.getImageUrls()))
                .visitCount(post.getVisitCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .spotName(post.getMetadata() != null ? post.getMetadata().getSpotName() : null)
                .location(post.getMetadata() != null ? post.getMetadata().getLocation() : null)
                .rating(post.getMetadata() != null ? post.getMetadata().getRating() : null)
                .spotId(post.getMetadata() != null ? post.getMetadata().getSpotId() : null)
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
                .spotName(this.spotName)
                .location(this.location)
                .rating(this.rating)
                .spotId(this.spotId)
                .build();
    }
}