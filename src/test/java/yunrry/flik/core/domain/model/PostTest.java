// test/java/yunrry/flik/core/domain/model/PostTest.java
package yunrry.flik.core.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("게시물 도메인 모델 테스트")
class PostTest {

    @Test
    @DisplayName("게시물 생성 시 필수 정보가 올바르게 설정된다")
    void shouldCreatePostWithRequiredFields() {
        // given & when
        Post post = Post.builder()
                .id(1L)
                .userId(123L)
                .type(PostType.REVIEW)
                .title("성수동 맛집 리뷰")
                .content("정말 맛있었어요")
                .imageUrls(List.of("https://example.com/image1.jpg"))
                .createdAt(LocalDateTime.now())
                .visitCount(0)
                .build();

        // then
        assertThat(post.getId()).isEqualTo(1L);
        assertThat(post.getUserId()).isEqualTo(123L);
        assertThat(post.getType()).isEqualTo(PostType.REVIEW);
        assertThat(post.getTitle()).isEqualTo("성수동 맛집 리뷰");
        assertThat(post.getContent()).isEqualTo("정말 맛있었어요");
        assertThat(post.getImageUrls()).hasSize(1);
        assertThat(post.getVisitCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("방문 횟수 증가가 정상적으로 동작한다")
    void shouldIncrementVisitCount() {
        // given
        Post post = Post.builder()
                .visitCount(5)
                .build();

        // when
        Post newPost = post.incrementVisitCount();

        // then
        assertThat(newPost.getVisitCount()).isEqualTo(6);
    }

    @Test
    @DisplayName("방문 횟수가 null일 때 1로 초기화된다")
    void shouldInitializeVisitCountWhenNull() {
        // given
        Post post = Post.builder()
                .visitCount(null)
                .build();

        // when
        Post newPost = post.incrementVisitCount();

        // then
        assertThat(newPost.getVisitCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("게시물 내용 수정이 정상적으로 동작한다")
    void shouldUpdateContent() {
        // given
        Post originalPost = Post.builder()
                .id(1L)
                .userId(123L)
                .type(PostType.REVIEW)
                .title("원본 제목")
                .content("원본 내용")
                .imageUrls(List.of("original.jpg"))
                .createdAt(LocalDateTime.now().minusDays(1))
                .visitCount(10)
                .build();

        // when
        Post updatedPost = originalPost.updateContent(
                "수정된 제목",
                "수정된 내용",
                List.of("updated.jpg")
        );

        // then
        assertThat(updatedPost.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedPost.getContent()).isEqualTo("수정된 내용");
        assertThat(updatedPost.getImageUrls()).containsExactly("updated.jpg");
        assertThat(updatedPost.getUpdatedAt()).isNotNull();
        assertThat(updatedPost.getVisitCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("게시물 소유권 확인이 정상적으로 동작한다")
    void shouldCheckOwnership() {
        // given
        Post post = Post.builder()
                .userId(123L)
                .build();

        // when & then
        assertThat(post.isOwnedBy(123L)).isTrue();
        assertThat(post.isOwnedBy(456L)).isFalse();
    }
}