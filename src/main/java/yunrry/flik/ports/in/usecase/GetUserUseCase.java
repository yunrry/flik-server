// ports/in/usecase/GetUserUseCase.java
package yunrry.flik.ports.in.usecase;

import yunrry.flik.core.domain.model.User;

public interface GetUserUseCase {
    User getUser(Long userId);
    String getUserNickName(Long userId);
}