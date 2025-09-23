package yunrry.flik.ports.in.query;

import lombok.Builder;
import lombok.Getter;
import yunrry.flik.core.domain.model.PostType;

@Getter
@Builder
public class SearchUserPostsQuery {
    private final int page;
    private final int size;
    private final String type;
    private final Long userId;

    public SearchUserPostsQuery(int page, int size, String type, Long userId) {
        this.page = Math.max(page, 0);
        this.size = (size <= 0 || size > 50) ? 20 : size;
        this.type = type;
        this.userId = userId;
    }
}