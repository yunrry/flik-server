package yunrry.flik.core.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import yunrry.flik.core.domain.exception.common.BaseErrorCode;
import yunrry.flik.core.domain.exception.common.ErrorReason;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements BaseErrorCode {

    POST_NOT_FOUND(404, "POST_NOT_FOUND", "게시물을 찾을 수 없습니다"),
    POST_ACCESS_DENIED(403, "POST_ACCESS_DENIED", "게시물에 대한 접근 권한이 없습니다"),
    INVALID_POST_TYPE(400, "INVALID_POST_TYPE", "유효하지 않은 게시물 타입입니다"),
    INVALID_POST_CONTENT(400, "INVALID_POST_CONTENT", "게시물 내용이 올바르지 않습니다");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status, code, message);
    }
}