package yunrry.flik.adapters.out.persistence.mysql.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import yunrry.flik.core.domain.model.Tag;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "embedding", columnDefinition = "TEXT")
    private String embedding; // JSON 형태로 저장: "[1.0, 2.0, 3.0, ...]"

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public TagEntity(Long id, String name, String embedding, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.embedding = embedding;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public static TagEntity from(Tag tag) {
        return TagEntity.builder()
                .id(tag.getId())
                .name(tag.getName())
                .embedding(convertEmbeddingToString(tag.getEmbedding()))
                .createdAt(tag.getCreatedAt())
                .build();
    }

    public Tag toDomain() {
        return Tag.builder()
                .id(this.id)
                .name(this.name)
                .embedding(parseEmbedding(this.embedding))
                .createdAt(this.createdAt)
                .build();
    }

    public void updateEmbedding(String embedding) {
        this.embedding = embedding;
    }

    public void updateEmbedding(List<Double> embedding) {
        this.embedding = convertEmbeddingToString(embedding);
    }

    private static String convertEmbeddingToString(List<Double> embedding) {
        if (embedding == null || embedding.isEmpty()) {
            return null;
        }
        return "[" + embedding.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + "]";
    }

    private List<Double> parseEmbedding(String embeddingString) {
        if (embeddingString == null || embeddingString.trim().isEmpty()) {
            return null;
        }

        try {
            String cleaned = embeddingString.replace("[", "").replace("]", "");
            return Arrays.stream(cleaned.split(","))
                    .map(String::trim)
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return null;
        }
    }
}