package yunrry.flik.adapters.out.persistence.postgres.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.UserCategoryVector;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 최적화된 UserCategoryVectorEntity
 * - String 대신 List<Double> 직접 사용으로 파싱 오버헤드 제거
 * - PostgreSQL vector 타입 직접 매핑
 * - 선호도 카운트 추가로 가중 평균 계산 지원
 */
@Entity
@Table(name = "user_category_vectors",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category"}),
        indexes = {
                @Index(name = "idx_user_category_vectors_user_category", columnList = "user_id, category"),
                @Index(name = "idx_user_category_vectors_category", columnList = "category")
        })
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

    @Type(PostgreSQLVectorType.class)
    @Column(name = "preference_vector", columnDefinition = "vector(1536)")
    private List<Double> preferenceVector;

    @Column(name = "preference_count", nullable = false)
    private Integer preferenceCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public UserCategoryVectorEntity(Long userId, String category, List<Double> preferenceVector, Integer preferenceCount) {
        this.userId = userId;
        this.category = category;
        this.preferenceVector = preferenceVector;
        this.preferenceCount = preferenceCount != null ? preferenceCount : 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateVector(List<Double> preferenceVector, Integer preferenceCount) {
        this.preferenceVector = preferenceVector;
        this.preferenceCount = preferenceCount != null ? preferenceCount : this.preferenceCount;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementPreferenceCount(int increment) {
        this.preferenceCount += increment;
        this.updatedAt = LocalDateTime.now();
    }

    // Domain conversion
    public UserCategoryVector toDomain() {
        return UserCategoryVector.builder()
                .id(this.id)
                .userId(this.userId)
                .category(MainCategory.findByCode(this.category))
                .preferenceVector(this.preferenceVector)
                .preferenceCount(this.preferenceCount)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public static UserCategoryVectorEntity fromDomain(UserCategoryVector domain) {
        return UserCategoryVectorEntity.builder()
                .userId(domain.getUserId())
                .category(domain.getCategory().getCode())
                .preferenceVector(domain.getPreferenceVector())
                .preferenceCount(domain.getPreferenceCount())
                .build();
    }
}
