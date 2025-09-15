package yunrry.flik.core.service.card;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.ports.in.query.SearchSpotsQuery;
import yunrry.flik.ports.in.usecase.SearchSpotsUseCase;
import yunrry.flik.ports.out.repository.SpotRepository;

@Service
@RequiredArgsConstructor
public class SearchSpotsService implements SearchSpotsUseCase {

    private final SpotRepository spotRepository;

    @Override
    @Cacheable(value = "spot-search", key = "#query.toCacheKey()")
    public Slice<Spot> searchSpots(SearchSpotsQuery query) {
        return spotRepository.findByConditions(query);
    }

}