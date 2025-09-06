package yunrry.flik.core.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@Builder
public class RefreshToken {
    private final String token;
    private final Long userId;
    private final LocalDateTime expiryTime;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }

    public boolean isValid() {
        return !isExpired();
    }
}