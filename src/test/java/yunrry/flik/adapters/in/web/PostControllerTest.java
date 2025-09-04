package yunrry.flik.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import yunrry.flik.config.SecurityConfig;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.core.domain.model.PostType;
import yunrry.flik.ports.in.usecase.*;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(PostController.class)
@DisplayName("게시물 컨트롤러 테스트")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreatePostUseCase createPostUseCase;

    @MockBean
    private GetPostUseCase getPostUseCase;

    @MockBean
    private UpdatePostUseCase updatePostUseCase;

    @MockBean
    private DeletePostUseCase deletePostUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("게시물 목록 조회가 인증 없이 성공한다")
    void shouldSearchPostsWithoutAuth() throws Exception {
        // given
        Post post = Post.builder()
                .id(1L)
                .userId(123L)
                .type(PostType.REVIEW)
                .title("성수동 맛집 리뷰")
                .content("정말 맛있었어요")
                .build();

        Slice<Post> slice = new SliceImpl<>(List.of(post));
        given(getPostUseCase.searchPosts(any())).willReturn(slice);

        // when & then - 인증 없이 요청
        mockMvc.perform(get("/v1/posts")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("게시물 상세 조회가 인증 없이 성공한다")
    void shouldGetPostWithoutAuth() throws Exception {
        // given
        Post post = Post.builder()
                .id(1L)
                .userId(123L)
                .type(PostType.REVIEW)
                .title("성수동 맛집 리뷰")
                .visitCount(5)
                .build();

        given(getPostUseCase.getPost(any())).willReturn(post);

        // when & then - 인증 없이 요청
        mockMvc.perform(get("/v1/posts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

//    @Test
//    @WithMockUser
//    @DisplayName("게시물 생성이 성공한다")
//    void shouldCreatePostSuccessfully() throws Exception {
//        // given
//        CreatePostRequest request = new CreatePostRequest(
//                "review",
//                "성수동 맛집 리뷰",
//                "정말 맛있었어요",
//                List.of("https://example.com/image1.jpg")
//        );
//
//        Post createdPost = Post.builder()
//                .id(1L)
//                .userId(123L)
//                .type(PostType.REVIEW)
//                .title("성수동 맛집 리뷰")
//                .content("정말 맛있었어요")
//                .build();
//
//        given(createPostUseCase.createPost(any())).willReturn(createdPost);
//
//        // when & then
//        mockMvc.perform(post("/v1/posts")
//                        .with(user("123"))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.id").value(1L))
//                .andExpect(jsonPath("$.data.message").exists());
//    }
}