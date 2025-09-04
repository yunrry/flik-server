package yunrry.flik.ports.in.query;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetSpotQuery {
    private final Long spotId;

    public Long getSpotId() {
        return spotId;
    }
}
