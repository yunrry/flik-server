package yunrry.flik.core.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import yunrry.flik.core.domain.exception.common.BaseErrorCode;
import yunrry.flik.core.domain.exception.common.ErrorReason;

@Getter
@RequiredArgsConstructor
public enum SpotErrorCode implements BaseErrorCode {

    SPOT_NOT_FOUND(404, "SPOT_NOT_FOUND", "장소를 찾을 수 없습니다"),
    SPOT_RUNNING_TIME_NULL(404, "SPOT_RUNNING_TIME_NULL", "영업 시간이 설정되지 않았습니다.");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status, code, message);
    }
}