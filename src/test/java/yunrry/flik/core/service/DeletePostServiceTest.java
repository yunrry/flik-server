package yunrry.flik.core.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yunrry.flik.core.domain.exception.PostAccessDeniedException;
import yunrry.flik.core.domain.exception.PostNotFoundException;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.ports.in.command.DeletePostCommand;
import yunrry.flik.ports.out.repository.PostRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("게시물 삭제 서비스 테스트")
class DeletePostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private DeletePostService deletePostService;

    @Test
    @DisplayName("게시물 삭제가 성공한다")
    void shouldDeletePostSuccessfully() {
        // given
        Long postId = 1L;
        Long userId = 123L;
        DeletePostCommand command = new DeletePostCommand(postId, userId);

        Post existingPost = Post.builder()
                .id(postId)
                .userId(userId)
                .build();

        given(postRepository.findById(postId)).willReturn(Optional.of(existingPost));

        // when
        deletePostService.deletePost(command);

        // then
        then(postRepository).should().findById(postId);
        then(postRepository).should().deleteById(postId);
    }

    @Test
    @DisplayName("존재하지 않는 게시물 삭제 시 예외가 발생한다")
    void shouldThrowExceptionWhenPostNotFound() {
        // given
        DeletePostCommand command = new DeletePostCommand(999L, 123L);
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deletePostService.deletePost(command))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("다른 사용자의 게시물 삭제 시 예외가 발생한다")
    void shouldThrowExceptionWhenAccessDenied() {
        // given
        Long postId = 1L;
        DeletePostCommand command = new DeletePostCommand(postId, 456L);

        Post existingPost = Post.builder()
                .id(postId)
                .userId(123L)
                .build();

        given(postRepository.findById(postId)).willReturn(Optional.of(existingPost));

        // when & then
        assertThatThrownBy(() -> deletePostService.deletePost(command))
                .isInstanceOf(PostAccessDeniedException.class);
    }
}