package yunrry.flik.ports.out.repository;

import org.springframework.data.domain.Slice;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.ports.in.query.SearchPostsQuery;
import yunrry.flik.ports.in.query.SearchUserPostsQuery;


import java.util.List;
import java.util.Optional;

public interface PostRepository {
    Post save(Post post);
    Optional<Post> findById(Long id);
    Slice<Post> findByConditions(SearchUserPostsQuery query);
    List<Post> findUserPostsAllByConditions(SearchUserPostsQuery query);
    List<Long> findUserPostIdsAll(Long userId);
    void deleteById(Long id);
    Slice<Post> findBySearchConditions(SearchPostsQuery query);
    Slice<Post> findBySearchUserConditions(SearchUserPostsQuery query);
}