//package yunrry.flik.core.service;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Slice;
//import org.springframework.data.domain.SliceImpl;
//import yunrry.flik.core.domain.exception.PostNotFoundException;
//import yunrry.flik.core.domain.model.Post;
//import yunrry.flik.core.domain.model.PostType;
//import yunrry.flik.core.service.post.GetPostService;
//import yunrry.flik.ports.in.query.GetPostQuery;
//import yunrry.flik.ports.in.query.SearchPostsQuery;
//import yunrry.flik.ports.out.repository.PostRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.BDDMockito.then;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("게시물 조회 서비스 테스트")
//class GetPostServiceTest {
//
//    @Mock
//    private PostRepository postRepository;
//
//    @InjectMocks
//    private GetPostService getPostService;
//
//    @Test
//    @DisplayName("게시물 단건 조회가 성공한다")
//    void shouldGetPostSuccessfully() {
//        // given
//        Long postId = 1L;
//        GetPostQuery query = new GetPostQuery(postId);
//
//        Post post = Post.builder()
//                .id(postId)
//                .userId(123L)
//                .type(PostType.REVIEW)
//                .title("성수동 맛집 리뷰")
//                .visitCount(5)
//                .build();
//
//        Post updatedPost = Post.builder()
//                .id(postId)
//                .userId(123L)
//                .type(PostType.REVIEW)
//                .title("성수동 맛집 리뷰")
//                .visitCount(6)
//                .build();
//
//        given(postRepository.findById(postId)).willReturn(Optional.of(post));
//        given(postRepository.save(post)).willReturn(updatedPost);
//
//        // when
//        Post result = getPostService.getPost(query);
//
//        // then
//        assertThat(result.getId()).isEqualTo(postId);
//        assertThat(result.getVisitCount()).isEqualTo(6);
//        then(postRepository).should().findById(postId);
//        then(postRepository).should().save(post);
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 게시물 조회 시 예외가 발생한다")
//    void shouldThrowExceptionWhenPostNotFound() {
//        // given
//        Long postId = 999L;
//        GetPostQuery query = new GetPostQuery(postId);
//
//        given(postRepository.findById(postId)).willReturn(Optional.empty());
//
//        // when & then
//        assertThatThrownBy(() -> getPostService.getPost(query))
//                .isInstanceOf(PostNotFoundException.class);
//
//        then(postRepository).should().findById(postId);
//    }
//
//    @Test
//    @DisplayName("게시물 검색이 성공한다")
//    void shouldSearchPostsSuccessfully() {
//        // given
//        SearchPostsQuery query = SearchPostsQuery.builder()
//                .page(0)
//                .size(20)
//                .type(PostType.REVIEW)
//                .userId(123L)
//                .build();
//
//        Post post = Post.builder()
//                .id(1L)
//                .userId(123L)
//                .type(PostType.REVIEW)
//                .title("리뷰 게시물")
//                .build();
//
//        Slice<Post> slice = new SliceImpl<>(List.of(post));
//        given(postRepository.findByConditions(query)).willReturn(slice);
//
//        // when
//        Slice<Post> result = getPostService.searchPosts(query);
//
//        // then
//        assertThat(result.getContent()).hasSize(1);
//        assertThat(result.getContent().get(0).getType()).isEqualTo(PostType.REVIEW);
//        then(postRepository).should().findByConditions(query);
//    }
//}