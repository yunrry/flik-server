package yunrry.flik.ports.in.usecase;

import yunrry.flik.core.domain.model.User;

public interface UpdateUserUseCase {
    User updateProfile(Long userId, String nickname, String profileImageUrl);
}