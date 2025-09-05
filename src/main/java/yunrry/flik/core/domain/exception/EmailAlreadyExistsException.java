package yunrry.flik.core.domain.exception;

import yunrry.flik.core.domain.exception.common.FlikException;

public class EmailAlreadyExistsException extends FlikException {

    public EmailAlreadyExistsException(String email) {
        super(UserErrorCode.EMAIL_ALREADY_EXISTS, "CORE");
    }
}
