package yunrry.flik.ports.out.repository;

import org.springframework.data.domain.Slice;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.ports.in.query.SearchPostsQuery;


import java.util.List;
import java.util.Optional;

public interface PostRepository {
    Post save(Post post);
    Optional<Post> findById(Long id);
    Slice<Post> findByConditions(SearchPostsQuery query);
    List<Post> findAllByConditions(SearchPostsQuery query);
    void deleteById(Long id);
}