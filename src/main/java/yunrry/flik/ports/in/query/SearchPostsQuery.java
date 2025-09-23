package yunrry.flik.ports.in.query;

import lombok.Builder;

@Builder
public record SearchPostsQuery(
        int page,
        int size,
        String type,
        String regionCode
) {}