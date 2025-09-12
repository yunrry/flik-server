package yunrry.flik.ports.out.repository;

import org.springframework.data.domain.Slice;
import yunrry.flik.core.domain.model.card.Restaurant;
import yunrry.flik.ports.in.query.SearchRestaurantsQuery;

import java.util.Optional;

public interface RestaurantRepository {
    Optional<Restaurant> findById(Long id);
    Slice<Restaurant> findByConditions(SearchRestaurantsQuery query);
}