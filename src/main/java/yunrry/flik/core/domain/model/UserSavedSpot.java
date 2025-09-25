package yunrry.flik.core.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class UserSavedSpot {
    private final Long id;
    private final Long userId;
    private final Long spotId;
    private final LocalDateTime createdAt;

}
