package yunrry.flik.ports.out.repository;

import org.springframework.data.domain.Slice;
import yunrry.flik.core.domain.model.Spot;
import yunrry.flik.ports.in.query.SearchSpotsQuery;

import java.util.Optional;

public interface SpotRepository {
    Optional<Spot> findById(Long id);
    Slice<Spot> findByConditions(SearchSpotsQuery query);
}