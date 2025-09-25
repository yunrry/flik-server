package yunrry.flik.adapters.in.dto;

import lombok.Builder;
import lombok.Getter;
import yunrry.flik.adapters.in.dto.spot.SpotDetailResponse;

import java.util.List;

@Builder
@Getter
public class SpotSearchResponsePaged {
    private String keyword;
    private int page;
    private int pageSize;
    private List<SpotDetailResponse> spots;
    private boolean hasNext;
    private int totalElements;
}