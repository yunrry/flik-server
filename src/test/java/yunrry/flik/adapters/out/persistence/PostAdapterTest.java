//package yunrry.flik.adapters.out.persistence;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//import org.springframework.context.annotation.Import;
//import org.springframework.data.domain.Slice;
//import yunrry.flik.adapters.out.persistence.mysql.PostAdapter;
//import yunrry.flik.adapters.out.persistence.mysql.entity.PostEntity;
//import yunrry.flik.core.domain.model.Post;
//import yunrry.flik.core.domain.model.PostType;
//import yunrry.flik.ports.in.query.SearchUserPostsQuery;
//
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//@Import(PostAdapter.class)
//@DisplayName("게시물 어댑터 테스트")
//class PostAdapterTest {
//
//    @Autowired
//    private PostAdapter postAdapter;
//
//    @Autowired
//    private TestEntityManager entityManager;
//
//    @Test
//    @DisplayName("게시물 저장이 성공한다")
//    void shouldSavePost() {
//        // given
//        Post post = Post.builder()
//                .userId(123L)
//                .type(PostType.REVIEW)
//                .title("테스트 제목")
//                .content("테스트 내용")
//                .imageUrls(List.of("https://example.com/image1.jpg"))
//                .createdAt(LocalDateTime.now())
//                .visitCount(0)
//                .build();
//
//        // when
//        Post savedPost = postAdapter.save(post);
//
//        // then
//        assertThat(savedPost.getId()).isNotNull();
//        assertThat(savedPost.getUserId()).isEqualTo(123L);
//        assertThat(savedPost.getType()).isEqualTo(PostType.REVIEW);
//        assertThat(savedPost.getTitle()).isEqualTo("테스트 제목");
//        assertThat(savedPost.getContent()).isEqualTo("테스트 내용");
//    }
//
//    @Test
//    @DisplayName("게시물 ID로 조회가 성공한다")
//    void shouldFindById() {
//        // given
//        PostEntity entity = PostEntity.builder()
//                .userId(123L)
//                .type(PostType.REVIEW)
//                .title("테스트 제목")
//                .content("테스트 내용")
//                .visitCount(0)
//                .createdAt(LocalDateTime.now())
//                .build();
//        PostEntity savedEntity = entityManager.persistAndFlush(entity);
//
//        // when
//        Optional<Post> foundPost = postAdapter.findById(savedEntity.getId());
//
//        // then
//        assertThat(foundPost).isPresent();
//        assertThat(foundPost.get().getTitle()).isEqualTo("테스트 제목");
//    }
//
//    @Test
//    @DisplayName("조건별 게시물 검색이 성공한다")
//    void shouldFindByConditions() {
//        // given
//        PostEntity reviewPost = PostEntity.builder()
//                .userId(123L)
//                .type(PostType.REVIEW)
//                .title("리뷰 게시물")
//                .content("리뷰 내용")
//                .visitCount(0)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        PostEntity savePost = PostEntity.builder()
//                .userId(123L)
//                .type(PostType.SAVE)
//                .title("저장 게시물")
//                .content("저장 내용")
//                .visitCount(0)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        entityManager.persist(reviewPost);
//        entityManager.persist(savePost);
//        entityManager.flush();
//
//        SearchUserPostsQuery query = SearchUserPostsQuery.builder()
//                .page(0)
//                .size(20)
//                .type(PostType.REVIEW)
//                .userId(123L)
//                .build();
//
//        // when
//        Slice<Post> result = postAdapter.findByConditions(query);
//
//        // then
//        assertThat(result.getContent()).hasSize(1);
//        assertThat(result.getContent().get(0).getType()).isEqualTo(PostType.REVIEW);
//    }
//
//    @Test
//    @DisplayName("게시물 삭제가 성공한다")
//    void shouldDeleteById() {
//        // given
//        PostEntity entity = PostEntity.builder()
//                .userId(123L)
//                .type(PostType.REVIEW)
//                .title("테스트 제목")
//                .content("테스트 내용")
//                .visitCount(0)
//                .createdAt(LocalDateTime.now())
//                .build();
//        PostEntity savedEntity = entityManager.persistAndFlush(entity);
//
//        // when
//        postAdapter.deleteById(savedEntity.getId());
//
//        // then
//        Optional<Post> foundPost = postAdapter.findById(savedEntity.getId());
//        assertThat(foundPost).isEmpty();
//    }
//}