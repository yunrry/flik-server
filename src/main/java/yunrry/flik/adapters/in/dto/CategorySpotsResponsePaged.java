package yunrry.flik.adapters.in.dto;

import lombok.Builder;
import lombok.Getter;
import yunrry.flik.core.domain.model.card.Spot;

import java.util.List;

@Getter
@Builder
public class CategorySpotsResponsePaged {
    private final int page;
    private final int pageSize;
    private final List<Spot> spots;
    private final boolean hasNext;
}
