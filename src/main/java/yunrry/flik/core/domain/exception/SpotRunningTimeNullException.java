package yunrry.flik.core.domain.exception;
import yunrry.flik.core.domain.exception.common.FlikException;

public class SpotRunningTimeNullException extends FlikException {

    public SpotRunningTimeNullException(Long spotId) {
        super(SpotErrorCode.SPOT_RUNNING_TIME_NULL, "CORE");
    }
}