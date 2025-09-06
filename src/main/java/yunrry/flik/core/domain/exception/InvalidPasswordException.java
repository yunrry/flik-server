package yunrry.flik.core.domain.exception;

import yunrry.flik.core.domain.exception.common.FlikException;

public class InvalidPasswordException extends FlikException {

    public InvalidPasswordException() {
        super(UserErrorCode.INVALID_PASSWORD, "CORE");
    }
}