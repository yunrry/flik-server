package yunrry.flik.ports.in.query;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetFestivalQuery {
    private final Long festivalId;

    public Long getFestivalId() {
        return festivalId;
    }
}
