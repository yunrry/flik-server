package yunrry.flik.ports.in.usecase;

import org.springframework.data.domain.Slice;
import yunrry.flik.core.domain.model.card.Festival;
import yunrry.flik.ports.in.query.SearchFestivalsQuery;

public interface SearchFestivalsUseCase {
    Slice<Festival> searchFestivals(SearchFestivalsQuery query);
}
