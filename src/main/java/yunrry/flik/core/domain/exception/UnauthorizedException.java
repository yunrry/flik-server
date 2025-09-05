// core/domain/exception/UnauthorizedException.java
package yunrry.flik.core.domain.exception;

import yunrry.flik.core.domain.exception.common.FlikException;

public class UnauthorizedException extends FlikException {

    public UnauthorizedException() {
        super(UserErrorCode.UNAUTHORIZED, "AUTH");
    }

    public UnauthorizedException(String message) {
        super(UserErrorCode.UNAUTHORIZED, "AUTH", message);
    }
}