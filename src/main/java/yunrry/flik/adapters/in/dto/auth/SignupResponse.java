package yunrry.flik.adapters.in.dto.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import yunrry.flik.core.domain.model.User;

import java.time.LocalDateTime;

public record SignupResponse(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        String provider,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
) {
    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getAuthProvider().getCode(),
                user.getCreatedAt()
        );
    }
}