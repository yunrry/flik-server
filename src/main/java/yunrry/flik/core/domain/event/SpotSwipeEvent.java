package yunrry.flik.core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class SpotSwipeEvent {
    private final Long userId;
    private final Long spotId;
    private final Instant timestamp;

    public static SpotSwipeEvent of(Long userId, Long spotId) {
        return new SpotSwipeEvent(userId, spotId, Instant.now());
    }
}