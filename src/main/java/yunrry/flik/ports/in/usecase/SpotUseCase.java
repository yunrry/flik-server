package yunrry.flik.ports.in.usecase;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import reactor.core.publisher.Mono;
import yunrry.flik.adapters.in.dto.spot.CategorySpotsResponse;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.card.Spot;

import yunrry.flik.ports.in.query.FindSpotsByCategoriesSliceQuery;
import yunrry.flik.ports.in.query.FindSpotsByCategoryQuery;
import yunrry.flik.ports.in.query.GetSpotQuery;

import java.util.List;

public interface SpotUseCase {
    Mono<Spot> findById(Long spotId);
    Mono<Void> updateSpotTags(Long spotId, List<String> keywords);

}

