package yunrry.flik.ports.in.usecase;

import org.springframework.data.domain.Slice;

import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.ports.in.query.SearchSpotsQuery;

public interface SearchSpotsUseCase {
    Slice<Spot> searchSpots(SearchSpotsQuery query);
}
