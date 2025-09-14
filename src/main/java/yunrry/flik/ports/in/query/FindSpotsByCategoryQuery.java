package yunrry.flik.ports.in.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import yunrry.flik.core.domain.model.MainCategory;

@Getter
@RequiredArgsConstructor
public class FindSpotsByCategoryQuery {
    private final MainCategory category;
    private final String regionCode;
    private final int limitPerCategory;
}