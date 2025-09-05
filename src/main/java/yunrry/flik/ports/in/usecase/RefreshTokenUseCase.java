package yunrry.flik.ports.in.usecase;

import yunrry.flik.ports.in.command.RefreshTokenCommand;

public interface RefreshTokenUseCase {
    AuthTokens refreshTokens(RefreshTokenCommand command);
}