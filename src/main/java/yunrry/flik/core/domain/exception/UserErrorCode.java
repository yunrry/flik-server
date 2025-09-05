package yunrry.flik.core.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import yunrry.flik.core.domain.exception.common.BaseErrorCode;
import yunrry.flik.core.domain.exception.common.ErrorReason;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    USER_NOT_FOUND(404, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다"),
    EMAIL_ALREADY_EXISTS(409, "EMAIL_ALREADY_EXISTS", "이미 존재하는 이메일입니다"),
    INVALID_PASSWORD(401, "INVALID_PASSWORD", "비밀번호가 올바르지 않습니다"),
    INVALID_TOKEN(401, "INVALID_TOKEN", "유효하지 않은 토큰입니다"),
    TOKEN_EXPIRED(401, "TOKEN_EXPIRED", "만료된 토큰입니다"),
    USER_DEACTIVATED(403, "USER_DEACTIVATED", "비활성화된 사용자입니다"),
    OAUTH_AUTHENTICATION_FAILED(401, "OAUTH_AUTHENTICATION_FAILED", "소셜 로그인 인증에 실패했습니다"),
    INVALID_PROVIDER(400, "INVALID_PROVIDER", "지원하지 않는 인증 제공자입니다"),
    OAUTH_SIGNUP_REQUIRED(200, "OAUTH_SIGNUP_REQUIRED", "소셜 로그인 회원가입이 필요합니다");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status, code, message);
    }
}