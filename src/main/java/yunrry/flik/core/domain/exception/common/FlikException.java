package yunrry.flik.core.domain.exception.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class FlikException extends RuntimeException {

    protected final BaseErrorCode errorCode;
    protected final String sourceLayer;

    public Integer getStatus() {
        return errorCode.getErrorReason().status();
    }

    public String getMessage() {
        if (sourceLayer == null) {
            return errorCode.getErrorReason().message();
        } else {
            return String.format("%s-%s", sourceLayer, errorCode.getErrorReason().message());
        }
    }

}
