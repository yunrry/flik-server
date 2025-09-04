package yunrry.flik.core.domain.exception;

import yunrry.flik.core.domain.exception.common.FlikException;

public class SpotNotFoundException extends FlikException {

    public SpotNotFoundException(Long spotId) {
        super(SpotErrorCode.SPOT_NOT_FOUND, "CORE");
    }
}