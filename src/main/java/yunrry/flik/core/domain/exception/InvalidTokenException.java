package yunrry.flik.core.domain.exception;

import yunrry.flik.core.domain.exception.common.FlikException;

public class InvalidTokenException extends FlikException {

    public InvalidTokenException() {
        super(UserErrorCode.INVALID_TOKEN, "CORE");
    }
}