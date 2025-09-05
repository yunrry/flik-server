package yunrry.flik.ports.in.usecase;

import yunrry.flik.core.domain.model.User;
import yunrry.flik.ports.in.command.LoginCommand;
import yunrry.flik.ports.in.command.OAuthLoginCommand;

public interface LoginUseCase {
    AuthTokens login(LoginCommand command);
    AuthTokens oauthLogin(OAuthLoginCommand command);
}
