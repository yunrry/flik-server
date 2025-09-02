package yunrry.flik.core.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import yunrry.flik.core.domain.exception.common.BaseErrorCode;
import yunrry.flik.core.domain.exception.common.ErrorReason;

@Getter
@RequiredArgsConstructor
public enum RestaurantErrorCode implements BaseErrorCode {

    RESTAURANT_NOT_FOUND(404, "RESTAURANT_NOT_FOUND", "음식점을 찾을 수 없습니다");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status, code, message);
    }
}