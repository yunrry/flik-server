package yunrry.flik.adapters.out.persistence.postgres.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @Column(name = "location_embedding", columnDefinition = "vector(2)")
    private String locationEmbedding; // [latitude, longitude] normalized

    @Column(name = "tag_embedding", columnDefinition = "vector(1536)")
    private String tagEmbedding; // OpenAI embedding for tags + categories

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public SpotEmbeddingEntity(Long spotId, String locationEmbedding, String tagEmbedding) {
        this.spotId = spotId;
        this.locationEmbedding = locationEmbedding;
        this.tagEmbedding = tagEmbedding;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateEmbeddings(String locationEmbedding, String tagEmbedding) {
        this.locationEmbedding = locationEmbedding;
        this.tagEmbedding = tagEmbedding;
        this.updatedAt = LocalDateTime.now();
    }

    // Domain conversion methods
    public SpotEmbedding toDomain() {
        return SpotEmbedding.builder()
                .id(this.id)
                .spotId(this.spotId)
                .locationEmbedding(parseVector(this.locationEmbedding))
                .tagEmbedding(parseVector(this.tagEmbedding))
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public static SpotEmbeddingEntity fromDomain(SpotEmbedding domain) {
        return SpotEmbeddingEntity.builder()
                .spotId(domain.getSpotId())
                .locationEmbedding(formatVector(domain.getLocationEmbedding()))
                .tagEmbedding(formatVector(domain.getTagEmbedding()))
                .build();
    }

    private List<Double> parseVector(String vectorString) {
        if (vectorString == null || vectorString.trim().isEmpty()) {
            return List.of();
        }

        try {
            String cleaned = vectorString.replace("[", "").replace("]", "");
            return Arrays.stream(cleaned.split(","))
                    .map(String::trim)
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    private static String formatVector(List<Double> vector) {
        if (vector == null || vector.isEmpty()) {
            return null;
        }
        return "[" + vector.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + "]";
    }
}