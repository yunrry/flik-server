package yunrry.flik.adapters.in.dto.restaurant;

import org.springframework.data.domain.Slice;
import yunrry.flik.adapters.in.dto.SliceSearchResponse;
import yunrry.flik.core.domain.model.Restaurant;

public record RestaurantSearchResponse()
{
    public static SliceSearchResponse<RestaurantDetailResponse> from(Slice<Restaurant> slice) {
        return SliceSearchResponse.from(slice, RestaurantDetailResponse::from);
    }
}

