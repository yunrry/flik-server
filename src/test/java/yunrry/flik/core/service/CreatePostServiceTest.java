package yunrry.flik.core.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.core.domain.model.PostType;
import yunrry.flik.ports.in.command.CreatePostCommand;
import yunrry.flik.ports.out.repository.PostRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("게시물 생성 서비스 테스트")
class CreatePostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CreatePostService createPostService;

    @Test
    @DisplayName("게시물 생성이 성공한다")
    void shouldCreatePostSuccessfully() {
        // given
        CreatePostCommand command = CreatePostCommand.builder()
                .userId(123L)
                .type(PostType.REVIEW)
                .title("성수동 맛집 리뷰")
                .content("정말 맛있었어요")
                .imageUrls(List.of("https://example.com/image1.jpg"))
                .build();

        Post savedPost = Post.builder()
                .id(1L)
                .userId(123L)
                .type(PostType.REVIEW)
                .title("성수동 맛집 리뷰")
                .content("정말 맛있었어요")
                .visitCount(0)
                .build();

        given(postRepository.save(any(Post.class))).willReturn(savedPost);

        // when
        Post result = createPostService.createPost(command);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(123L);
        assertThat(result.getType()).isEqualTo(PostType.REVIEW);
        assertThat(result.getTitle()).isEqualTo("성수동 맛집 리뷰");
        assertThat(result.getContent()).isEqualTo("정말 맛있었어요");
        assertThat(result.getVisitCount()).isEqualTo(0);
        then(postRepository).should().save(any(Post.class));
    }

    @Test
    @DisplayName("제목이 비어있으면 예외가 발생한다")
    void shouldThrowExceptionWhenTitleIsEmpty() {
        // given
        CreatePostCommand command = CreatePostCommand.builder()
                .userId(123L)
                .type(PostType.REVIEW)
                .title("")
                .content("내용")
                .build();

        // when & then
        assertThatThrownBy(() -> createPostService.createPost(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("제목은 필수입니다");
    }

    @Test
    @DisplayName("내용이 비어있으면 예외가 발생한다")
    void shouldThrowExceptionWhenContentIsEmpty() {
        // given
        CreatePostCommand command = CreatePostCommand.builder()
                .userId(123L)
                .type(PostType.REVIEW)
                .title("제목")
                .content("")
                .build();

        // when & then
        assertThatThrownBy(() -> createPostService.createPost(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("내용은 필수입니다");
    }
}