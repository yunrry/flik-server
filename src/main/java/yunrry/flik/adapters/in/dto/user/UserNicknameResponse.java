package yunrry.flik.adapters.in.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import yunrry.flik.core.domain.model.User;

import java.time.LocalDateTime;

public record UserNicknameResponse(
        String nickname
) {
    public static UserNicknameResponse from(String nickname) {
        return new UserNicknameResponse(
                nickname
        );
    }
}