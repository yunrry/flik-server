package yunrry.flik.ports.out.repository;

import org.springframework.data.domain.Slice;
import yunrry.flik.core.domain.model.card.Festival;
import yunrry.flik.ports.in.query.SearchFestivalsQuery;

import java.util.Optional;

public interface FestivalRepository {
    Optional<Festival> findById(Long id);
    Slice<Festival> findByConditions(SearchFestivalsQuery query);
}