package yunrry.flik.core.service.spot;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.exception.FestivalNotFoundException;
import yunrry.flik.core.domain.model.card.Festival;
import yunrry.flik.ports.in.query.GetFestivalQuery;
import yunrry.flik.ports.in.usecase.FestivalUseCase;
import yunrry.flik.ports.out.repository.FestivalRepository;

@Service
@RequiredArgsConstructor
public class GetFestivalService implements FestivalUseCase {

    private final FestivalRepository festivalRepository;

    @Override
    @Cacheable(value = "festivals", key = "#query.festivalId")
    public Festival getFestival(GetFestivalQuery query) {
        return festivalRepository.findById(query.getFestivalId())
                .orElseThrow(() -> new FestivalNotFoundException(query.getFestivalId()));
    }
}