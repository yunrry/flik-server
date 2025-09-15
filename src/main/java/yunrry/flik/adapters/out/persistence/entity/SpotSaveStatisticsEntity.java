package yunrry.flik.adapters.out.persistence.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "spot_save_statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpotSaveStatisticsEntity {

    @Id
    @Column(name = "spot_id")
    private Long spotId;

    @Column(name = "save_count", nullable = false)
    private Integer saveCount;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public SpotSaveStatisticsEntity(Long spotId, Integer saveCount, LocalDateTime updatedAt) {
        this.spotId = spotId;
        this.saveCount = saveCount != null ? saveCount : 0;
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public static SpotSaveStatisticsEntity of(Long spotId) {
        return SpotSaveStatisticsEntity.builder()
                .spotId(spotId)
                .saveCount(0)
                .build();
    }

    public void incrementSaveCount() {
        this.saveCount++;
        this.updatedAt = LocalDateTime.now();
    }
}