package yunrry.flik.ports.in.usecase.post;

import org.springframework.data.domain.Slice;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.ports.in.query.GetPostQuery;
import yunrry.flik.ports.in.query.SearchPostsQuery;

public interface GetPostUseCase {
    Post getPost(GetPostQuery query);
    Slice<Post> searchPosts(SearchPostsQuery query);
}