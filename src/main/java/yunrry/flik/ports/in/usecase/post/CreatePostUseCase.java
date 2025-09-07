package yunrry.flik.ports.in.usecase.post;

import yunrry.flik.core.domain.model.Post;
import yunrry.flik.ports.in.command.CreatePostCommand;

public interface CreatePostUseCase {
    Post createPost(CreatePostCommand command);
}