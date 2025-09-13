package yunrry.flik.ports.in.usecase;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.card.Spot;

import yunrry.flik.ports.in.query.FindSpotsByCategoriesSliceQuery;
import yunrry.flik.ports.in.query.FindSpotsByCategoryQuery;
import yunrry.flik.ports.in.query.GetSpotQuery;

import java.util.List;

public interface SpotUseCase {
    Spot getSpot(GetSpotQuery query);
    List<Spot> findSpotsByCategories(List<MainCategory> categories, String regionCode, int limitPerCategory);
    Slice<Spot> findSpotsByCategoriesSlice(FindSpotsByCategoriesSliceQuery query);
}

