package yunrry.flik.adapters.out.persistence.postgres.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import yunrry.flik.core.domain.model.embedding.SpotEmbedding;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "spot_embeddings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpotEmbeddingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spot_id", nullable = false, unique = true)
    private Long spotId;

    @Type(PostgreSQLVectorType.class)
    @Column(name = "location_embedding", columnDefinition = "vector(2)")
    private List<Double> locationEmbedding; // [latitude, longitude] normalized

    @Type(PostgreSQLVectorType.class)
    @Column(name = "tag_embedding", columnDefinition = "vector(1536)")
    private List<Double> tagEmbedding;// OpenAI embedding for tags + categories

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public SpotEmbeddingEntity(Long spotId, List<Double> locationEmbedding, List<Double> tagEmbedding) {
        this.spotId = spotId;
        this.locationEmbedding = locationEmbedding;
        this.tagEmbedding = tagEmbedding;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateEmbeddings(List<Double> locationEmbedding, List<Double> tagEmbedding) {
        this.locationEmbedding = locationEmbedding;
        this.tagEmbedding = tagEmbedding;
        this.updatedAt = LocalDateTime.now();
    }

    // Domain conversion methods
    public SpotEmbedding toDomain() {
        return SpotEmbedding.builder()
                .id(this.id)
                .spotId(this.spotId)
                .locationEmbedding(this.locationEmbedding) // 직접 사용
                .tagEmbedding(this.tagEmbedding)
                .build();
    }

    public static SpotEmbeddingEntity fromDomain(SpotEmbedding domain) {
        return SpotEmbeddingEntity.builder()
                .spotId(domain.getSpotId())
                .locationEmbedding(domain.getLocationEmbedding()) // 직접 사용
                .tagEmbedding(domain.getTagEmbedding())
                .build();
    }

}