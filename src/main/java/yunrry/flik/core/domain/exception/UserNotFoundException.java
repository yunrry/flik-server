package yunrry.flik.core.domain.exception;

import yunrry.flik.core.domain.exception.common.FlikException;

public class UserNotFoundException extends FlikException {

    public UserNotFoundException(Long userId) {
        super(UserErrorCode.USER_NOT_FOUND, "CORE");
    }

    public UserNotFoundException(String email) {
        super(UserErrorCode.USER_NOT_FOUND, "CORE");
    }
}