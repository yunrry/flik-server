package yunrry.flik.core.domain.exception;
import yunrry.flik.core.domain.exception.common.FlikException;

public class RestaurantRunningTimeNullException extends FlikException {

    public RestaurantRunningTimeNullException(Long restaurantId) {
        super(RestaurantErrorCode.RESTAURANT_RUNNING_TIME_NULL, "CORE");
    }
}