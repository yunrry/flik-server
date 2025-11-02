package yunrry.flik.core.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.exception.UserNotFoundException;
import yunrry.flik.core.domain.model.User;
import yunrry.flik.ports.in.usecase.GetUserUseCase;
import yunrry.flik.ports.out.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class GetUserService implements GetUserUseCase {

    private final UserRepository userRepository;

    @Override
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    public String getUserNickName(Long userId){
        return userRepository.findNickNameById(userId);
    }
}