package yunrry.flik.adapters.out.persistence.mysql;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import yunrry.flik.adapters.out.persistence.mysql.entity.RestaurantEntity;
import yunrry.flik.adapters.out.persistence.mysql.repository.RestaurantJpaRepository;
import yunrry.flik.core.domain.model.card.Restaurant;
import yunrry.flik.ports.in.query.SearchRestaurantsQuery;
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

    @Override
    public Slice<Restaurant> findByConditions(SearchRestaurantsQuery query) {
        Pageable pageable = createPageable(query);

        Slice<RestaurantEntity> entities = restaurantJpaRepository.findByConditions(
                query.getCategory(),
                query.getKeyword(),
                query.getAddress(),
                pageable
        );

        return entities.map(entity -> entity.toDomain());
    }

    private Pageable createPageable(SearchRestaurantsQuery query) {
        Sort sort = createSort(query.getSort());
        return PageRequest.of(query.getPage(), query.getSize(), sort);
    }

    private Sort createSort(String sortBy) {
        return switch (sortBy) {
            case "rating" -> Sort.by(Sort.Direction.DESC, "rating");
            case "distance" -> Sort.by(Sort.Direction.ASC, "distance");
            case "name" -> Sort.by(Sort.Direction.ASC, "name");
            default -> Sort.by(Sort.Direction.ASC, "distance");
        };
    }
}