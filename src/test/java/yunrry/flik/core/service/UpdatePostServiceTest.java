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
import yunrry.flik.core.domain.model.PostType;
import yunrry.flik.ports.in.command.UpdatePostCommand;
import yunrry.flik.ports.out.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("게시물 수정 서비스 테스트")
class UpdatePostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private UpdatePostService updatePostService;

    @Test
    @DisplayName("게시물 수정이 성공한다")
    void shouldUpdatePostSuccessfully() {
        // given
        Long postId = 1L;
        Long userId = 123L;

        UpdatePostCommand command = UpdatePostCommand.builder()
                .postId(postId)
                .userId(userId)
                .title("수정된 제목")
                .content("수정된 내용")
                .imageUrls(List.of("updated.jpg"))
                .build();

        Post existingPost = Post.builder()
                .id(postId)
                .userId(userId)
                .type(PostType.REVIEW)
                .title("원본 제목")
                .content("원본 내용")
                .createdAt(LocalDateTime.now())
                .visitCount(10)
                .build();

        Post updatedPost = Post.builder()
                .id(postId)
                .userId(userId)
                .type(PostType.REVIEW)
                .title("수정된 제목")
                .content("수정된 내용")
                .imageUrls(List.of("updated.jpg"))
                .updatedAt(LocalDateTime.now())
                .visitCount(10)
                .build();

        given(postRepository.findById(postId)).willReturn(Optional.of(existingPost));
        given(postRepository.save(any(Post.class))).willReturn(updatedPost);

        // when
        Post result = updatePostService.updatePost(command);

        // then
        assertThat(result.getTitle()).isEqualTo("수정된 제목");
        assertThat(result.getContent()).isEqualTo("수정된 내용");
        assertThat(result.getImageUrls()).containsExactly("updated.jpg");
        then(postRepository).should().findById(postId);
        then(postRepository).should().save(any(Post.class));
    }

    @Test
    @DisplayName("존재하지 않는 게시물 수정 시 예외가 발생한다")
    void shouldThrowExceptionWhenPostNotFound() {
        // given
        UpdatePostCommand command = UpdatePostCommand.builder()
                .postId(999L)
                .userId(123L)
                .title("수정된 제목")
                .content("수정된 내용")
                .build();

        given(postRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> updatePostService.updatePost(command))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("다른 사용자의 게시물 수정 시 예외가 발생한다")
    void shouldThrowExceptionWhenAccessDenied() {
        // given
        Long postId = 1L;
        UpdatePostCommand command = UpdatePostCommand.builder()
                .postId(postId)
                .userId(456L)  // 다른 사용자
                .title("수정된 제목")
                .content("수정된 내용")
                .build();

        Post existingPost = Post.builder()
                .id(postId)
                .userId(123L)  // 원래 작성자
                .build();

        given(postRepository.findById(postId)).willReturn(Optional.of(existingPost));

        // when & then
        assertThatThrownBy(() -> updatePostService.updatePost(command))
                .isInstanceOf(PostAccessDeniedException.class);
    }
}