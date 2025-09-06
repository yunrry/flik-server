package yunrry.flik.ports.in.usecase;

import lombok.Builder;
import lombok.Getter;
import yunrry.flik.core.domain.model.User;

@Getter
@Builder
public class AuthTokens {
    private final String accessToken;
    private final String refreshToken;
    private final User user;
}