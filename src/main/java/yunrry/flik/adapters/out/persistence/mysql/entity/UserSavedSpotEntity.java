package yunrry.flik.adapters.out.persistence.mysql.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import yunrry.flik.core.domain.model.UserSavedSpot;

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

    public UserSavedSpotEntity(Long id, Long userId, Long spotId, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.spotId = spotId;
        this.createdAt = createdAt;
    }



    public UserSavedSpot toDomain() {
        return new UserSavedSpot (
                this.getId(),
                this.getUserId(),
                this.getSpotId(),
                this.getCreatedAt()
                );
    }

    public static UserSavedSpotEntity of(Long userId, Long spotId) {
        return new UserSavedSpotEntity(null, userId, spotId, LocalDateTime.now());
    }

    public UserSavedSpotEntity toEntity(UserSavedSpot spot) {
        return new UserSavedSpotEntity(
                spot.getId(),
                spot.getUserId(),
                spot.getSpotId(),
                spot.getCreatedAt() != null ? spot.getCreatedAt() : LocalDateTime.now()
        );
    }

}