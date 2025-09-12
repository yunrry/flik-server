package yunrry.flik.ports.in.usecase;

import yunrry.flik.core.domain.model.card.Festival;
import yunrry.flik.ports.in.query.GetFestivalQuery;

public interface FestivalUseCase {
    Festival getFestival(GetFestivalQuery query);
}
