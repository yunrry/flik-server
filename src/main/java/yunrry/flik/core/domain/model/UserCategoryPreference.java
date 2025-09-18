package yunrry.flik.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCategoryPreference {

    private Long id;
    private Long userId;
    private MainCategory mainCategory;
    private SubCategory subCategory;
    private DetailCategory detailCategory;
    private Double preferenceScore;
    private Integer saveCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void incrementPreference(Double increment) {
        this.preferenceScore += increment;
        this.saveCount++;
        this.updatedAt = LocalDateTime.now();
    }
}