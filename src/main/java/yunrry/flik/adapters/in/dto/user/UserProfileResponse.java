package yunrry.flik.adapters.in.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import yunrry.flik.core.domain.model.User;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        String provider,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime lastLoginAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getAuthProvider().getCode(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }
}