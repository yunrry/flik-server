package yunrry.flik.ports.in.usecase;

import org.springframework.data.domain.Slice;
import reactor.core.publisher.Mono;
import yunrry.flik.adapters.in.dto.spot.CategorySpotsResponse;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.ports.in.query.FindSpotsByCategoriesSliceQuery;
import yunrry.flik.ports.in.query.GetSpotQuery;

import java.util.List;

public interface GetSpotUseCase {
    Spot getSpot(GetSpotQuery query);
    List<Spot> findSpotsByCategories(List<MainCategory> categories, String regionCode, int limitPerCategory, int tripDuration);
    Slice<Spot> findSpotsByCategoriesSlice(FindSpotsByCategoriesSliceQuery query);
    CategorySpotsResponse findSpotsByCategoriesWithCacheKey(List<MainCategory> categories, String regionCode, int limitPerCategory, int tripDuration);
}

