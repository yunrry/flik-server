package yunrry.flik.ports.in.query;

import lombok.Builder;
import lombok.Getter;
import yunrry.flik.core.domain.model.PostType;

@Getter
@Builder
public class SearchPostsQuery {
    private final int page;
    private final int size;
    private final PostType type;
    private final Long userId;

    public SearchPostsQuery(int page, int size, PostType type, Long userId) {
        this.page = Math.max(page, 0);
        this.size = (size <= 0 || size > 50) ? 20 : size;
        this.type = type;
        this.userId = userId;
    }
}