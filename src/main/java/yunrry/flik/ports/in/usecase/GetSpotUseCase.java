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
    List<Spot> findSpotsByIds(List<Long> spotIds);
    List<Spot> findSpotsByCategories(List<MainCategory> categories, String regionCode, int limitPerCategory, int tripDuration, Long userId);
    Slice<Spot> findSpotsByCategoriesSlice(FindSpotsByCategoriesSliceQuery query);
    CategorySpotsResponse findSpotsByCategoriesWithCacheKey(List<MainCategory> categories, String regionCode, int limitPerCategory, int tripDuration, Long userId);
    List<Spot> getSpotsByCategoriesPaged(
            List<MainCategory> categories,
            String regionCode,
            int limitPerCategory,
            Long userId,
            int pageNumber);

    List<Spot> getUserSavedSpots(Long userId);
}

