package yunrry.flik.adapters.in.dto.festival;

import org.springframework.data.domain.Slice;
import yunrry.flik.adapters.in.dto.SliceSearchResponse;
import yunrry.flik.core.domain.model.Festival;

public record FestivalSearchResponse(
) {
    public static SliceSearchResponse<FestivalDetailResponse> from(Slice<Festival> slice) {
        return SliceSearchResponse.from(slice, FestivalDetailResponse::from);
    }
}