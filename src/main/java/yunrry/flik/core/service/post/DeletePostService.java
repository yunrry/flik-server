package yunrry.flik.core.service.post;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.exception.PostAccessDeniedException;
import yunrry.flik.core.domain.exception.PostNotFoundException;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.ports.in.command.DeletePostCommand;
import yunrry.flik.ports.in.usecase.post.DeletePostUseCase;
import yunrry.flik.ports.out.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class DeletePostService implements DeletePostUseCase {

    private final PostRepository postRepository;

    @Override
    @CacheEvict(value = "posts", key = "#command.postId")
    public void deletePost(DeletePostCommand command) {
        Post post = postRepository.findById(command.getPostId())
                .orElseThrow(() -> new PostNotFoundException(command.getPostId()));

        if (!post.isOwnedBy(command.getUserId())) {
            throw new PostAccessDeniedException(command.getUserId(), command.getPostId());
        }

        postRepository.deleteById(command.getPostId());
    }
}