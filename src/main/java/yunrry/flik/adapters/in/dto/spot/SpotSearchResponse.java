package yunrry.flik.adapters.in.dto.spot;

import org.springframework.data.domain.Slice;
import yunrry.flik.adapters.in.dto.SliceSearchResponse;
import yunrry.flik.core.domain.model.card.Spot;


public record SpotSearchResponse(
) {
    public static SliceSearchResponse<SpotDetailResponse> from(Slice<Spot> slice) {
        return SliceSearchResponse.from(slice, SpotDetailResponse::from);
    }
}