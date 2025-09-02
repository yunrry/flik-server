package yunrry.flik.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import yunrry.flik.adapters.out.persistence.repository.RestaurantJpaRepository;
import yunrry.flik.core.domain.model.Restaurant;
import yunrry.flik.ports.out.repository.RestaurantRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RestaurantAdapter implements RestaurantRepository {

    private final RestaurantJpaRepository restaurantJpaRepository;

    @Override
    public Optional<Restaurant> findById(Long id) {
        return restaurantJpaRepository.findById(id)
                .map(entity -> entity.toDomain());
    }
}