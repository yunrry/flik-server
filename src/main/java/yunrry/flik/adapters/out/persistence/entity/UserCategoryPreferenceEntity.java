package yunrry.flik.adapters.out.persistence.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import yunrry.flik.core.domain.mapper.CategoryMapper;
import yunrry.flik.core.domain.model.DetailCategory;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.SubCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_category_preferences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCategoryPreferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "main_category", nullable = false)
    private String mainCategory;

    @Column(name = "sub_category", nullable = false)
    private String subCategory;

    @Column(name = "detail_category", nullable = false, length = 50)
    private String detailCategory;


    @Column(name = "preference_score", nullable = false)
    private Double preferenceScore;

    @Column(name = "save_count", nullable = false)
    private Integer saveCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public UserCategoryPreferenceEntity(Long userId, String mainCategory, String subCategory, String detailCategory, Double preferenceScore,
                                        Integer saveCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.mainCategory = mainCategory;
        this.subCategory = subCategory;
        this.detailCategory = detailCategory;
        this.preferenceScore = preferenceScore != null ? preferenceScore : 0.0;
        this.saveCount = saveCount != null ? saveCount : 0;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public static UserCategoryPreferenceEntity of(Long userId, String detailCategory) {

        SubCategory subCategory = CategoryMapper.getSubCategory(DetailCategory.findByKoreanName(detailCategory));
        MainCategory mainCategory = CategoryMapper.getMainCategory(subCategory);

        String subCategoryName = subCategory.getKoreanName();
        String mainCategoryName = mainCategory.getDisplayName();

        return UserCategoryPreferenceEntity.builder()
                .userId(userId)
                .mainCategory(subCategoryName)
                .subCategory(mainCategoryName)
                .detailCategory(detailCategory)
                .preferenceScore(0.0)
                .saveCount(0)
                .build();
    }

    public void incrementPreference(Double increment) {
        this.preferenceScore += increment;
        this.saveCount++;
        this.updatedAt = LocalDateTime.now();
    }
}