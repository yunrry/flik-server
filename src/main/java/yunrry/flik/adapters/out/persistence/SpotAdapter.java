package yunrry.flik.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import yunrry.flik.adapters.out.persistence.entity.SpotEntity;
import yunrry.flik.adapters.out.persistence.repository.SpotJpaRepository;
import yunrry.flik.core.domain.model.Spot;
import yunrry.flik.ports.in.query.SearchSpotsQuery;
import yunrry.flik.ports.out.repository.SpotRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SpotAdapter implements SpotRepository {

    private final SpotJpaRepository spotJpaRepository;

    @Override
    public Optional<Spot> findById(Long id) {
        return spotJpaRepository.findById(id)
                .map(entity -> entity.toDomain());
    }

    @Override
    public Slice<Spot> findByConditions(SearchSpotsQuery query) {
        Pageable pageable = createPageable(query);

        Slice<SpotEntity> entities = spotJpaRepository.findByConditions(
                query.getCategory(),
                query.getKeyword(),
                query.getAddress(),
                pageable
        );

        return entities.map(entity -> entity.toDomain());
    }

    private Pageable createPageable(SearchSpotsQuery query) {
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