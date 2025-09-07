package yunrry.flik.adapters.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yunrry.flik.adapters.in.dto.Response;
import yunrry.flik.adapters.in.dto.post.*;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.core.domain.model.PostType;
import yunrry.flik.ports.in.command.CreatePostCommand;
import yunrry.flik.ports.in.command.DeletePostCommand;
import yunrry.flik.ports.in.command.UpdatePostCommand;
import yunrry.flik.ports.in.query.GetPostQuery;
import yunrry.flik.ports.in.query.SearchPostsQuery;
import yunrry.flik.ports.in.usecase.post.CreatePostUseCase;
import yunrry.flik.ports.in.usecase.post.DeletePostUseCase;
import yunrry.flik.ports.in.usecase.post.GetPostUseCase;
import yunrry.flik.ports.in.usecase.post.UpdatePostUseCase;

@Tag(name = "Post", description = "사용자 활동 게시물 API")
@RestController
@RequestMapping("/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final CreatePostUseCase createPostUseCase;
    private final GetPostUseCase getPostUseCase;
    private final UpdatePostUseCase updatePostUseCase;
    private final DeletePostUseCase deletePostUseCase;

    @Operation(summary = "게시물 목록 조회", description = "사용자 활동 게시물 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<Response<PostSearchResponse>> searchPosts(
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "게시물 타입", example = "review")
            @RequestParam(required = false) String type,

            @Parameter(description = "사용자 ID")
            @RequestParam(required = false) Long userId) {

        PostType postType = type != null ? PostType.fromCode(type) : null;

        SearchPostsQuery query = SearchPostsQuery.builder()
                .page(page)
                .size(size)
                .type(postType)
                .userId(userId)
                .build();

        Slice<Post> posts = getPostUseCase.searchPosts(query);
        PostSearchResponse response = PostSearchResponse.from(posts);

        return ResponseEntity.ok(Response.success(response));
    }

    @Operation(summary = "게시물 상세 조회", description = "게시물 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<Response<ActivityDetailPostResponse>> getPost(@PathVariable Long id) {
        GetPostQuery query = new GetPostQuery(id);
        Post post = getPostUseCase.getPost(query);
        ActivityDetailPostResponse response = ActivityDetailPostResponse.from(post);

        return ResponseEntity.ok(Response.success(response));
    }

    @Operation(summary = "게시물 생성", description = "새로운 활동 게시물을 생성합니다.")
    @PostMapping
    public ResponseEntity<Response<CreatePostResponse>> createPost(
            @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal Long userId) {

        CreatePostCommand command = CreatePostCommand.builder()
                .userId(userId)
                .type(PostType.fromCode(request.type()))
                .title(request.title())
                .content(request.content())
                .imageUrls(request.imageUrl())
                .build();

        Post post = createPostUseCase.createPost(command);
        CreatePostResponse response = CreatePostResponse.from(post);

        return ResponseEntity.ok(Response.success(response));
    }

    @Operation(summary = "게시물 수정", description = "기존 게시물을 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<Response<UpdatePostResponse>> updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal Long userId) {

        UpdatePostCommand command = UpdatePostCommand.builder()
                .postId(id)
                .userId(userId)
                .title(request.title())
                .content(request.content())
                .imageUrls(request.imageUrl())
                .build();

        Post post = updatePostUseCase.updatePost(command);
        UpdatePostResponse response = UpdatePostResponse.from(post);

        return ResponseEntity.ok(Response.success(response));
    }

    @Operation(summary = "게시물 삭제", description = "게시물을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {

        DeletePostCommand command = new DeletePostCommand(id, userId);
        deletePostUseCase.deletePost(command);

        return ResponseEntity.ok(Response.success(null));
    }
}