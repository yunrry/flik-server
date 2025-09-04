package yunrry.flik.core.domain.exception;

import yunrry.flik.core.domain.exception.common.FlikException;

public class FestivalRunningTimeNullException extends FlikException {
    public FestivalRunningTimeNullException(Long festivalId) {
        super(FestivalErrorCode.FESTIVAL_RUNNING_TIME_NULL, "CORE");
    }
}
