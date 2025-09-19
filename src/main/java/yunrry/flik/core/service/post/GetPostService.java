package yunrry.flik.core.service.post;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import yunrry.flik.adapters.in.dto.post.PostSearchResponse;
import yunrry.flik.adapters.in.dto.post.UserActivityPostResponse;
import yunrry.flik.adapters.out.persistence.mysql.entity.PostEntity;
import yunrry.flik.core.domain.exception.PostNotFoundException;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.core.domain.model.PostType;
import yunrry.flik.ports.in.query.GetPostQuery;
import yunrry.flik.ports.in.query.SearchPostsQuery;
import yunrry.flik.ports.in.usecase.post.GetPostUseCase;
import yunrry.flik.ports.out.repository.PostRepository;

import java.util.List;

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

//    @Override
//    @Cacheable(value = "post-search",
//            key = "#query.page + '_' + #query.size + '_' + #query.type + '_' + #query.userId")
//    public List<Post> searchPosts(SearchPostsQuery query) {
//        return postRepository.findByConditions(query);
//    }


    @Override
    public PostSearchResponse getUserPosts(Long userId, String typeCode, int page, int size) {

        PostType postType = typeCode != null ? PostType.fromCode(typeCode) : null;

        SearchPostsQuery query = SearchPostsQuery.builder()
                .page(page)
                .size(size)
                .type(postType)
                .userId(userId)
                .build();

        Slice<Post> postsSlice = postRepository.findByConditions(query);

        List<UserActivityPostResponse> content = postsSlice.getContent().stream()
                .map(UserActivityPostResponse::from)
                .toList();

        return new PostSearchResponse(
                content,
                new PostSearchResponse.PageableInfo(page, size),
                postsSlice.hasNext(),
                postsSlice.getNumberOfElements()
        );
    }
}