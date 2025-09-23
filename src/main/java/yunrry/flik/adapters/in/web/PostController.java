package yunrry.flik.adapters.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yunrry.flik.adapters.in.dto.Response;
import yunrry.flik.adapters.in.dto.post.*;
import yunrry.flik.core.domain.model.Post;
import yunrry.flik.core.domain.model.PostType;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.core.domain.model.plan.TravelCourse;
import yunrry.flik.ports.in.command.CreatePostCommand;
import yunrry.flik.ports.in.command.DeletePostCommand;
import yunrry.flik.ports.in.command.UpdatePostCommand;
import yunrry.flik.ports.in.query.GetPostQuery;
import yunrry.flik.ports.in.query.SearchPostsQuery;
import yunrry.flik.ports.in.query.SearchUserPostsQuery;
import yunrry.flik.ports.in.usecase.GetSpotUseCase;
import yunrry.flik.ports.in.usecase.TravelCourseUseCase;
import yunrry.flik.ports.in.usecase.post.CreatePostUseCase;
import yunrry.flik.ports.in.usecase.post.DeletePostUseCase;
import yunrry.flik.ports.in.usecase.post.GetPostUseCase;
import yunrry.flik.ports.in.usecase.post.UpdatePostUseCase;

import java.util.List;

@Slf4j
@Tag(name = "Post", description = "사용자 활동 게시물 API")
@RestController
@RequestMapping("/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final CreatePostUseCase createPostUseCase;
    private final GetPostUseCase getPostUseCase;
    private final UpdatePostUseCase updatePostUseCase;
    private final DeletePostUseCase deletePostUseCase;
    private final TravelCourseUseCase travelCourseUseCase;
    private final GetSpotUseCase getSpotUseCase;


    @GetMapping
    public ResponseEntity<Response<PostSearchResponse>> getAllMyPosts(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "게시물 타입 (선택)")
            @RequestParam(required = false) String type) {

        log.debug("Searching posts: user={} page={}, size={}, type={}",
                userId, page, size, type);

        SearchUserPostsQuery query = SearchUserPostsQuery.builder()
                .page(page)
                .size(size)
                .type(type)
                .userId(userId)
                .build();

        Slice<Post> postSlice = getPostUseCase.searchUserPosts(query);

        List<ActivityDetailPostResponse> content = postSlice.getContent().stream()
                .map(post -> {
                    TravelCourse travelCourse = post.getCourseId() != null
                            ? travelCourseUseCase.getTravelCourse(post.getCourseId())
                            : null;

                    List<Spot> spots = (post.getSpotIds() != null && !post.getSpotIds().isEmpty())
                            ? getSpotUseCase.findSpotsByIds(post.getSpotIds())
                            : null;

                    return ActivityDetailPostResponse.from(post, travelCourse, spots);
                })
                .toList();

        PostSearchResponse response = PostSearchResponse.builder()
                .content(content)
                .pageable(PostSearchResponse.PageableInfo.builder()
                        .pageNumber(postSlice.getNumber())
                        .pageSize(postSlice.getSize())
                        .build())
                .hasNext(postSlice.hasNext())
                .numberOfElements(postSlice.getNumberOfElements())
                .build();

        return ResponseEntity.ok(Response.success(response));
    }

    @Operation(summary = "게시물 상세 조회", description = "게시물 상세 정보를 조회합니다.")
    @GetMapping("/get/{id}")
    public ResponseEntity<Response<ActivityDetailPostResponse>> getPost(@PathVariable Long id) {
        log.debug("Fetching post detail for id: {}", id);

        GetPostQuery query = new GetPostQuery(id);
        Post post = getPostUseCase.getPost(query);
        log.debug("Retrieved post: id={}, courseId={}, spotIds={}",
                post.getId(), post.getCourseId(), post.getSpotIds());

        TravelCourse travelCourse = post.getCourseId() != null
                ? travelCourseUseCase.getTravelCourse(post.getCourseId())
                : null;
        log.debug("Retrieved travelCourse: {}", travelCourse != null ? travelCourse.getId() : "null");

        List<Spot> spots = (post.getSpotIds() != null && !post.getSpotIds().isEmpty())
                ? getSpotUseCase.findSpotsByIds(post.getSpotIds())
                : null;
        log.debug("Retrieved spots count: {}", spots != null ? spots.size() : 0);

        ActivityDetailPostResponse response = ActivityDetailPostResponse.from(post, travelCourse, spots);
        log.debug("Created response for post id: {}", id);

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
                .regionCode(request.regionCode())
                .spotIds(request.spotIds())
                .relatedSpotIds(request.relatedSpotIds())
                .courseId(request.courseId())
                .build();

        Post post = createPostUseCase.createPost(command);
        CreatePostResponse response = CreatePostResponse.from(post);

        return ResponseEntity.ok(Response.success(response));
    }

    @Operation(summary = "게시물 수정", description = "기존 게시물을 수정합니다.")
    @PutMapping("{id}")
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
    @DeleteMapping("{id}")
    public ResponseEntity<Response<Void>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {

        DeletePostCommand command = new DeletePostCommand(id, userId);
        deletePostUseCase.deletePost(command);

        return ResponseEntity.ok(Response.success(null));
    }


    @Operation(summary = "게시물 검색", description = "페이징 처리된 게시물 목록을 조회합니다.")
    @GetMapping("/search")
    public ResponseEntity<Response<PostSearchResponse>> searchPosts(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "게시물 타입 (선택)")
            @RequestParam(required = false) String type,
            @Parameter(description = "지역 코드 (선택)")
            @RequestParam(required = false) String regionCode) {

        log.debug("Searching posts: page={}, size={}, type={}, regionCode={}",
                page, size, type, regionCode);

        SearchPostsQuery query = SearchPostsQuery.builder()
                .page(page)
                .size(size)
                .type(type)
                .regionCode(regionCode)
                .build();

        Slice<Post> postSlice = getPostUseCase.searchPosts(query);

        List<ActivityDetailPostResponse> content = postSlice.getContent().stream()
                .map(post -> {
                    TravelCourse travelCourse = post.getCourseId() != null
                            ? travelCourseUseCase.getTravelCourse(post.getCourseId())
                            : null;

                    List<Spot> spots = (post.getSpotIds() != null && !post.getSpotIds().isEmpty())
                            ? getSpotUseCase.findSpotsByIds(post.getSpotIds())
                            : null;

                    return ActivityDetailPostResponse.from(post, travelCourse, spots);
                })
                .toList();

        PostSearchResponse response = PostSearchResponse.builder()
                .content(content)
                .pageable(PostSearchResponse.PageableInfo.builder()
                        .pageNumber(postSlice.getNumber())
                        .pageSize(postSlice.getSize())
                        .build())
                .hasNext(postSlice.hasNext())
                .numberOfElements(postSlice.getNumberOfElements())
                .build();

        return ResponseEntity.ok(Response.success(response));
    }

}