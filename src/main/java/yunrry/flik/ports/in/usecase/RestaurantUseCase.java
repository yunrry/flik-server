package yunrry.flik.ports.in.usecase;

import yunrry.flik.core.domain.model.Restaurant;
import yunrry.flik.ports.in.query.GetRestaurantQuery;

public interface RestaurantUseCase {
    Restaurant getRestaurant(GetRestaurantQuery query);
}
