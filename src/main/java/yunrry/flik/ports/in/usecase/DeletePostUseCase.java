package yunrry.flik.ports.in.usecase;

import yunrry.flik.ports.in.command.DeletePostCommand;

public interface DeletePostUseCase {
    void deletePost(DeletePostCommand command);
}
