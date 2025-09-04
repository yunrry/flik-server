package yunrry.flik.ports.in.usecase;

import yunrry.flik.core.domain.model.Spot;
import yunrry.flik.ports.in.query.GetSpotQuery;

public interface SpotUseCase {
    Spot getSpot(GetSpotQuery query);
}

