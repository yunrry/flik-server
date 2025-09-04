package yunrry.flik.core.service.post;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.exception.PostNotFoundException;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.ports.in.query.GetPostQuery;
import yunrry.flik.ports.in.query.SearchPostsQuery;
import yunrry.flik.ports.in.usecase.GetPostUseCase;
import yunrry.flik.ports.out.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class GetPostService implements GetPostUseCase {

    private final PostRepository postRepository;

    @Override
    @Cacheable(value = "posts", key = "#query.postId")
    public Post getPost(GetPostQuery query) {
        Post post = postRepository.findById(query.getPostId())
                .orElseThrow(() -> new PostNotFoundException(query.getPostId()));

        post.incrementVisitCount();
        return postRepository.save(post);
    }

    @Override
    @Cacheable(value = "post-search",
            key = "#query.page + '_' + #query.size + '_' + #query.type + '_' + #query.userId")
    public Slice<Post> searchPosts(SearchPostsQuery query) {
        return postRepository.findByConditions(query);
    }
}