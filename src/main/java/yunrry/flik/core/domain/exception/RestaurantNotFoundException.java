package yunrry.flik.core.domain.exception;

import yunrry.flik.core.domain.exception.common.FlikException;

public class RestaurantNotFoundException extends FlikException {

    public RestaurantNotFoundException(Long restaurantId) {
        super(RestaurantErrorCode.RESTAURANT_NOT_FOUND, "CORE");
    }
}