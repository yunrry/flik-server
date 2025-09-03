package yunrry.flik.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.model.Restaurant;
import yunrry.flik.ports.in.query.SearchRestaurantsQuery;
import yunrry.flik.ports.in.usecase.SearchRestaurantsUseCase;
import yunrry.flik.ports.out.repository.RestaurantRepository;

@Service
@RequiredArgsConstructor
public class SearchRestaurantsService implements SearchRestaurantsUseCase {

    private final RestaurantRepository restaurantRepository;

    @Override
    @Cacheable(value = "restaurant-search",
            key = "#query.page + '_' + #query.size + '_' + #query.category + '_' + #query.sort + '_' + #query.keyword + '_' + #query.address")
    public Slice<Restaurant> searchRestaurants(SearchRestaurantsQuery query) {
        return restaurantRepository.findByConditions(query);
    }

}