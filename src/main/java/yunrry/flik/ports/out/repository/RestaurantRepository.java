package yunrry.flik.ports.out.repository;

import yunrry.flik.core.domain.model.Restaurant;

import java.util.Optional;

public interface RestaurantRepository {
    Optional<Restaurant> findById(Long id);
}