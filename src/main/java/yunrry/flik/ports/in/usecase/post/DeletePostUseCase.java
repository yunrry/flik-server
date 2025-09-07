package yunrry.flik.ports.in.usecase.post;

import yunrry.flik.ports.in.command.DeletePostCommand;

public interface DeletePostUseCase {
    void deletePost(DeletePostCommand command);
}
