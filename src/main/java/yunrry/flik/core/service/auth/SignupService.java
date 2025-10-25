package yunrry.flik.core.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.exception.EmailAlreadyExistsException;
import yunrry.flik.core.domain.model.AuthProvider;
import yunrry.flik.core.domain.model.User;
import yunrry.flik.ports.in.command.SignupCommand;
import yunrry.flik.ports.in.usecase.SignupUseCase;
import yunrry.flik.ports.out.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SignupService implements SignupUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User signup(SignupCommand command) {
        command.validate();

        if (userRepository.existsByEmail(command.getEmail())) {
            throw new EmailAlreadyExistsException(command.getEmail());
        }

        String encodedPassword = passwordEncoder.encode(command.getPassword());

        User user = User.builder()
                .email(command.getEmail())
                .password(encodedPassword)
                .nickname(command.getNickname())
                .profileImageUrl(command.getProfileImageUrl())
                .isGuest(command.isGuest())
                .authProvider(AuthProvider.EMAIL)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .lastLoginAt(null)
                .build();

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId){
        userRepository.deleteById(userId);
    }
}