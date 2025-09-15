package yunrry.flik.adapters.out.persistence.mysql.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_saved_spots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSavedSpotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "spot_id", nullable = false)
    private Long spotId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public UserSavedSpotEntity(Long userId, Long spotId, LocalDateTime createdAt) {
        this.userId = userId;
        this.spotId = spotId;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public static UserSavedSpotEntity of(Long userId, Long spotId) {
        return UserSavedSpotEntity.builder()
                .userId(userId)
                .spotId(spotId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}