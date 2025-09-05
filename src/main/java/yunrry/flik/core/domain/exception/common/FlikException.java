package yunrry.flik.core.domain.exception.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class FlikException extends RuntimeException {

    protected final BaseErrorCode errorCode;
    protected final String sourceLayer;
    protected final String customMessage;

    // 기존 생성자 (하위 호환성 유지)
    public FlikException(BaseErrorCode errorCode, String sourceLayer) {
        this.errorCode = errorCode;
        this.sourceLayer = sourceLayer;
        this.customMessage = null;
    }

    public Integer getStatus() {
        return errorCode.getErrorReason().status();
    }

    @Override
    public String getMessage() {
        String baseMessage = customMessage != null ? customMessage : errorCode.getErrorReason().message();

        if (sourceLayer == null) {
            return baseMessage;
        } else {
            return String.format("%s-%s", sourceLayer, baseMessage);
        }
    }
}