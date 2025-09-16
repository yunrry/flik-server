package yunrry.flik.adapters.out.persistence.postgres.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.UserCategoryVector;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "user_category_vectors",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCategoryVectorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "preference_vector", columnDefinition = "vector(1536)")
    private String preferenceVector;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public UserCategoryVectorEntity(Long userId, String category, String preferenceVector) {
        this.userId = userId;
        this.category = category;
        this.preferenceVector = preferenceVector;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateVector(String preferenceVector) {
        this.preferenceVector = preferenceVector;
        this.updatedAt = LocalDateTime.now();
    }

    // Domain conversion
    public UserCategoryVector toDomain() {
        return UserCategoryVector.builder()
                .id(this.id)
                .userId(this.userId)
                .category(MainCategory.findByCode(this.category))
                .preferenceVector(parseVector(this.preferenceVector))
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public static UserCategoryVectorEntity fromDomain(UserCategoryVector domain) {
        return UserCategoryVectorEntity.builder()
                .userId(domain.getUserId())
                .category(domain.getCategory().getCode())
                .preferenceVector(formatVector(domain.getPreferenceVector()))
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