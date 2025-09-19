package yunrry.flik.ports.in.usecase.post;

import org.springframework.data.domain.Slice;
import yunrry.flik.adapters.in.dto.post.PostSearchResponse;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.ports.in.query.GetPostQuery;
import yunrry.flik.ports.in.query.SearchPostsQuery;

import java.util.List;

public interface GetPostUseCase {
    Post getPost(GetPostQuery query);
    PostSearchResponse getUserPosts(Long userId, String typeCode, int page, int size);

}