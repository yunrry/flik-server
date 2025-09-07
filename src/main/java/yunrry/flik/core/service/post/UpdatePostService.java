package yunrry.flik.core.service.post;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.exception.PostAccessDeniedException;
import yunrry.flik.core.domain.exception.PostNotFoundException;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.ports.in.command.UpdatePostCommand;
import yunrry.flik.ports.in.usecase.post.UpdatePostUseCase;
import yunrry.flik.ports.out.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class UpdatePostService implements UpdatePostUseCase {

    private final PostRepository postRepository;

    @Override
    @CacheEvict(value = "posts", key = "#command.postId")
    public Post updatePost(UpdatePostCommand command) {
        command.validate();

        Post existingPost = postRepository.findById(command.getPostId())
                .orElseThrow(() -> new PostNotFoundException(command.getPostId()));

        if (!existingPost.isOwnedBy(command.getUserId())) {
            throw new PostAccessDeniedException(command.getUserId(), command.getPostId());
        }

        Post updatedPost = existingPost.updateContent(
                command.getTitle(),
                command.getContent(),
                command.getImageUrls()
        );

        return postRepository.save(updatedPost);
    }
}