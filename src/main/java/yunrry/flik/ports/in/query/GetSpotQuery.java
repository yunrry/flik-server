package yunrry.flik.ports.in.query;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
public class GetSpotQuery {
    private final Long spotId;

    public Long getSpotId() {
        return spotId;
    }
}
