package yunrry.flik.core.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.exception.UserNotFoundException;
import yunrry.flik.core.domain.model.User;
import yunrry.flik.ports.in.usecase.UpdateUserUseCase;
import yunrry.flik.ports.out.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UpdateUserService implements UpdateUserUseCase {

    private final UserRepository userRepository;

    @Override
    public User updateProfile(Long userId, String nickname, String profileImageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        User updatedUser = user.updateProfile(nickname, profileImageUrl);
        return userRepository.save(updatedUser);
    }
}