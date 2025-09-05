package yunrry.flik.ports.in.usecase;

import yunrry.flik.core.domain.model.OAuthUserInfo;
import yunrry.flik.ports.in.command.CompleteOAuthSignupCommand;
import yunrry.flik.ports.in.command.OAuthLoginCommand;

public interface OAuthSignupUseCase {
    OAuthUserInfo getOAuthUserInfo(OAuthLoginCommand command);
    AuthTokens completeOAuthSignup(CompleteOAuthSignupCommand command);
}