package yunrry.flik.adapters.in.dto.auth;

public record UserInfo(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        String provider
) {
    public static UserInfo from(yunrry.flik.core.domain.model.User user) {
        return new UserInfo(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getAuthProvider().getCode()
        );
    }
}
