package yunrry.flik.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.model.Festival;
import yunrry.flik.ports.in.query.SearchFestivalsQuery;
import yunrry.flik.ports.in.usecase.SearchFestivalsUseCase;
import yunrry.flik.ports.out.repository.FestivalRepository;

@Service
@RequiredArgsConstructor
public class FestivalsService implements SearchFestivalsUseCase {

    private final FestivalRepository festivalRepository;

    @Override
    @Cacheable(value = "festival-search",
            key = "#query.page + '_' + #query.size + '_' + #query.category + '_' + #query.sort + '_' + #query.keyword + '_' + #query.address")
    public Slice<Festival> searchFestivals(SearchFestivalsQuery query) {
        return festivalRepository.findByConditions(query);
    }

}