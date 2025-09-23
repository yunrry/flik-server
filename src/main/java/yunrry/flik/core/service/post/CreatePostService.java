package yunrry.flik.core.service.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.core.domain.model.PostMetadata;
import yunrry.flik.core.domain.model.User;
import yunrry.flik.core.service.user.GetUserService;
import yunrry.flik.ports.in.command.CreatePostCommand;
import yunrry.flik.ports.in.usecase.GetUserUseCase;
import yunrry.flik.ports.in.usecase.post.CreatePostUseCase;
import yunrry.flik.ports.out.repository.PostRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreatePostService implements CreatePostUseCase {

    private final PostRepository postRepository;
    private final GetUserUseCase getUserUseCase;

    @Override
    public Post createPost(CreatePostCommand command) {
        command.validate();
        User author = getUserUseCase.getUser(command.getUserId());
        Post post = Post.builder()
                .userId(command.getUserId())
                .userNickname(author.getNickname())
                .userProfileImageUrl(author.getProfileImageUrl())
                .type(command.getType())
                .title(command.getTitle())
                .content(command.getContent())
                .imageUrls(command.getImageUrls())
                .metadata(PostMetadata.of(command.getRegionCode(), command.getRelatedSpotIds(), command.getCourseId()))
                .createdAt(LocalDateTime.now())
                .visitCount(0)
                .spotIds(command.getSpotIds())
                .courseId(command.getCourseId())
                .build();

        return postRepository.save(post);
    }
}