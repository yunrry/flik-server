package yunrry.flik.core.service.card;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.exception.SpotNotFoundException;

import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.ports.in.query.GetSpotQuery;
import yunrry.flik.ports.in.usecase.SpotUseCase;
import yunrry.flik.ports.out.repository.SpotRepository;

@Service
@RequiredArgsConstructor
public class GetSpotService implements SpotUseCase {

    private final SpotRepository spotRepository;

    @Override
    @Cacheable(value = "spots", key = "#query.spotId")
    public Spot getSpot(GetSpotQuery query) {
        return spotRepository.findById(query.getSpotId())
                .orElseThrow(() -> new SpotNotFoundException(query.getSpotId()));
    }
}