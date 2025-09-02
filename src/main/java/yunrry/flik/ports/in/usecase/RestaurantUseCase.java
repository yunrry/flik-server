package yunrry.flik.ports.in.usecase;

import yunrry.flik.core.domain.model.Restaurant;

public interface RestaurantUseCase {
    Restaurant getRestaurant(GetRestaurantQuery query);
}
