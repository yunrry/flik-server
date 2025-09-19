package yunrry.flik.adapters.in.dto;


import lombok.Builder;
import lombok.Data;
import yunrry.flik.adapters.in.dto.spot.SpotDetailResponse;

import java.util.List;

@Data
@Builder
public class RandomSpotsResponse {
    private int page;
    private int pageSize;
    private List<SpotDetailResponse> spots;
    private boolean hasNext;
}