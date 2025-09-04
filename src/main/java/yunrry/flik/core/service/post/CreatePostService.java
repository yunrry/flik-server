package yunrry.flik.core.service.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.core.domain.model.PostMetadata;
import yunrry.flik.ports.in.command.CreatePostCommand;
import yunrry.flik.ports.in.usecase.CreatePostUseCase;
import yunrry.flik.ports.out.repository.PostRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreatePostService implements CreatePostUseCase {

    private final PostRepository postRepository;

    @Override
    public Post createPost(CreatePostCommand command) {
        command.validate();

        Post post = Post.builder()
                .userId(command.getUserId())
                .type(command.getType())
                .title(command.getTitle())
                .content(command.getContent())
                .imageUrls(command.getImageUrls())
                .metadata(PostMetadata.empty())
                .createdAt(LocalDateTime.now())
                .visitCount(0)
                .build();

        return postRepository.save(post);
    }
}