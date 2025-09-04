package yunrry.flik.core.service.card;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.exception.RestaurantNotFoundException;
import yunrry.flik.core.domain.model.Restaurant;
import yunrry.flik.ports.in.query.GetRestaurantQuery;
import yunrry.flik.ports.in.usecase.RestaurantUseCase;
import yunrry.flik.ports.out.repository.RestaurantRepository;

@Service
@RequiredArgsConstructor
public class GetRestaurantService implements RestaurantUseCase {

    private final RestaurantRepository restaurantRepository;

    @Override
    @Cacheable(value = "restaurants", key = "#query.restaurantId")
    public Restaurant getRestaurant(GetRestaurantQuery query) {
        return restaurantRepository.findById(query.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(query.getRestaurantId()));
    }
}