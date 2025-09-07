package yunrry.flik.ports.in.usecase.post;


import yunrry.flik.core.domain.model.Post;
import yunrry.flik.ports.in.command.UpdatePostCommand;

public interface UpdatePostUseCase {
    Post updatePost(UpdatePostCommand command);
}