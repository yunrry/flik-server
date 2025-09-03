package yunrry.flik.ports.in.usecase;

import org.springframework.data.domain.Slice;
import yunrry.flik.core.domain.model.Restaurant;
import yunrry.flik.ports.in.query.SearchRestaurantsQuery;

public interface SearchRestaurantsUseCase {
    Slice<Restaurant> searchRestaurants(SearchRestaurantsQuery query);
}