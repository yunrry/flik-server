package yunrry.flik.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import yunrry.flik.adapters.out.persistence.entity.PostEntity;
import yunrry.flik.adapters.out.persistence.repository.PostJpaRepository;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.ports.in.query.SearchPostsQuery;
import yunrry.flik.ports.out.repository.PostRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostAdapter implements PostRepository {

    private final PostJpaRepository postJpaRepository;

    @Override
    public Post save(Post post) {
        PostEntity entity = PostEntity.fromDomain(post);
        PostEntity savedEntity = postJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Post> findById(Long id) {
        return postJpaRepository.findById(id)
                .map(PostEntity::toDomain);
    }

    @Override
    public Slice<Post> findByConditions(SearchPostsQuery query) {
        Pageable pageable = createPageable(query);

        Slice<PostEntity> entities = postJpaRepository.findByConditions(
                query.getType(),
                query.getUserId(),
                pageable
        );

        return entities.map(PostEntity::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        postJpaRepository.deleteById(id);
    }

    private Pageable createPageable(SearchPostsQuery query) {
        return PageRequest.of(query.getPage(), query.getSize());
    }
}