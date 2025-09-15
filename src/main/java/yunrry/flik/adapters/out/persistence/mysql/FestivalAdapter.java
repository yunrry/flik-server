package yunrry.flik.adapters.out.persistence.mysql;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import yunrry.flik.adapters.out.persistence.mysql.entity.FestivalEntity;
import yunrry.flik.adapters.out.persistence.mysql.repository.FestivalJpaRepository;
import yunrry.flik.core.domain.model.card.Festival;
import yunrry.flik.ports.in.query.SearchFestivalsQuery;
import yunrry.flik.ports.out.repository.FestivalRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FestivalAdapter implements FestivalRepository {

    private final FestivalJpaRepository festivalJpaRepository;

    @Override
    public Optional<Festival> findById(Long id) {
        return festivalJpaRepository.findById(id)
                .map(entity -> entity.toDomain());
    }

    @Override
    public Slice<Festival> findByConditions(SearchFestivalsQuery query) {
        Pageable pageable = createPageable(query);

        Slice<FestivalEntity> entities = festivalJpaRepository.findByConditions(
                query.getCategory(),
                query.getKeyword(),
                query.getAddress(),
                pageable
        );

        return entities.map(entity -> entity.toDomain());
    }

    private Pageable createPageable(SearchFestivalsQuery query) {
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