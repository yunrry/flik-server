package yunrry.flik.core.domain.exception;

import yunrry.flik.core.domain.exception.common.FlikException;

public class FestivalNotFoundException extends FlikException {

    public FestivalNotFoundException(Long spotId) {
        super(FestivalErrorCode.FESTIVAL_NOT_FOUND, "CORE");
    }
}