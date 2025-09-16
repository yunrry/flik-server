package yunrry.flik.core.domain.model.embedding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotEmbedding {

    private Long id;
    private Long spotId;
    private List<Double> locationEmbedding;
    private List<Double> tagEmbedding;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean hasLocationEmbedding() {
        return locationEmbedding != null && !locationEmbedding.isEmpty();
    }

    public boolean hasTagEmbedding() {
        return tagEmbedding != null && !tagEmbedding.isEmpty();
    }

    public boolean isComplete() {
        return hasLocationEmbedding() && hasTagEmbedding();
    }

    public String getLocationEmbeddingAsString() {
        return formatVector(locationEmbedding);
    }

    public String getTagEmbeddingAsString() {
        return formatVector(tagEmbedding);
    }

    private String formatVector(List<Double> vector) {
        if (vector == null || vector.isEmpty()) {
            return null;
        }
        return "[" + vector.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(",")) + "]";
    }
}
