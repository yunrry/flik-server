package yunrry.flik.core.domain.exception.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RequestErrorCode implements BaseErrorCode {


    INVALID_INPUT(400, "BAD_REQUEST", "잘못된 요청입니다. 파라미터 값을 확인해주세요."),;

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status, code, message);
    }
}