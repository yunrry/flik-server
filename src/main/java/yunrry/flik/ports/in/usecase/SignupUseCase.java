package yunrry.flik.ports.in.usecase;

import yunrry.flik.core.domain.model.User;
import yunrry.flik.ports.in.command.SignupCommand;

public interface SignupUseCase {
    User signup(SignupCommand command);
    void deleteUser(Long userId);
}