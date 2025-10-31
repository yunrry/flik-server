package yunrry.flik.adapters.out.persistence.mysql;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import yunrry.flik.adapters.out.persistence.mysql.entity.PostEntity;
import yunrry.flik.adapters.out.persistence.mysql.repository.PostJpaRepository;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.core.domain.model.PostType;
import yunrry.flik.ports.in.query.SearchPostsQuery;
import yunrry.flik.ports.in.query.SearchUserPostsQuery;
import yunrry.flik.ports.out.repository.PostRepository;

import java.util.List;
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
    public Slice<Post> findByConditions(SearchUserPostsQuery query) {
        Pageable pageable = createPageable(query);
        PostType postType = query.getType() != null ? PostType.fromCode(query.getType()) : null;
        Slice<PostEntity> entities = postJpaRepository.findByConditions(
                postType,
                query.getUserId(),
                pageable
        );

        return entities.map(PostEntity::toDomain);
    }

    @Override
    public List<Post> findUserPostsAllByConditions(SearchUserPostsQuery query) {
        PostType postType = query.getType() != null ? PostType.fromCode(query.getType()) : null;

        List<PostEntity> entities = postJpaRepository.findAllByConditions(
                postType,
                query.getUserId()
        );

        return entities.stream()
                .map(PostEntity::toDomain)
                .toList();
    }

    @Override
    public List<Long> findUserPostIdsAll(Long userId) {
        return postJpaRepository.findAllIds(userId);
    }


    @Override
    public void deleteById(Long id) {
        postJpaRepository.deleteById(id);
    }


    @Override
    public Slice<Post> findBySearchConditions(SearchPostsQuery query) {
        PageRequest pageRequest = PageRequest.of(query.page(), query.size());
        PostType postType = query.type() != null ? PostType.fromCode(query.type()) : null;

        return postJpaRepository.findBySearchConditions(
                postType,
                query.regionCode(),
                pageRequest
        ).map(PostEntity::toDomain);
    }

    @Override
    public Slice<Post> findBySearchUserConditions(SearchUserPostsQuery query) {
        PageRequest pageRequest = PageRequest.of(query.getPage(), query.getSize());
        PostType postType = query.getType() != null ? PostType.fromCode(query.getType()) : null;

        return postJpaRepository.findBySearchUserConditions(
                postType,
                query.getUserId(),
                pageRequest
        ).map(PostEntity::toDomain);
    }


    private Pageable createPageable(SearchUserPostsQuery query) {
        return PageRequest.of(query.getPage(), query.getSize());
    }
}