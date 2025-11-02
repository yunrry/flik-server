package yunrry.flik.ports.in.usecase.post;

import org.springframework.data.domain.Slice;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.ports.in.query.GetPostQuery;
import yunrry.flik.ports.in.query.SearchPostsQuery;
import yunrry.flik.ports.in.query.SearchUserPostsQuery;

import java.util.List;

public interface GetPostUseCase {
    Post getPost(GetPostQuery query);
    Slice<Post> getUserPosts(Long userId, String typeCode, int page, int size);
    List<Post> getAllUserPosts(Long userId, String typeCode);
    Slice<Post> searchPosts(SearchPostsQuery query);
    Slice<Post> searchUserPosts(SearchUserPostsQuery query);
    List<Long> getAllUserPostIds(Long userId);
}