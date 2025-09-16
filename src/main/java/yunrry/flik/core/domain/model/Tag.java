package yunrry.flik.core.domain.model;

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
public class Tag {

    private Long id;
    private String name;
    private List<Double> embedding;
    private LocalDateTime createdAt;

    public static Tag of(String name) {
        return Tag.builder()
                .name(name)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Tag of(String name, List<Double> embedding) {
        return Tag.builder()
                .name(name)
                .embedding(embedding)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public Tag withEmbedding(List<Double> embedding) {
        return Tag.builder()
                .id(this.id)
                .name(this.name)
                .embedding(embedding)
                .createdAt(this.createdAt)
                .build();
    }

    public boolean hasEmbedding() {
        return embedding != null && !embedding.isEmpty();
    }
}