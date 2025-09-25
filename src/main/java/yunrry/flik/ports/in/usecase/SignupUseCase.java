package yunrry.flik.ports.in.usecase;

import yunrry.flik.core.domain.model.User;

public interface SignupUseCase {
    User signup(yunrry.flik.ports.in.usecase.SignupCommand command);
    void deleteUser(Long userId);
}