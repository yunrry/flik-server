package yunrry.flik.adapters.in.dto.spot;

import yunrry.flik.core.domain.model.card.Spot;

import java.util.List;

public class CategorySpotsResponse {
    private List<Spot> spots;
    private String cacheKey;

    public CategorySpotsResponse(List<Spot> spots, String cacheKey) {
        this.spots = spots;
        this.cacheKey = cacheKey;
    }

    // getters
    public List<Spot> getSpots() { return spots; }
    public String getCacheKey() { return cacheKey; }
}