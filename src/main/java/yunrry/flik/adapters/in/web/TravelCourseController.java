package yunrry.flik.adapters.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import yunrry.flik.adapters.in.dto.Response;
import yunrry.flik.adapters.in.dto.TravelCourseResponse;
import yunrry.flik.adapters.in.dto.TravelCourseUpdateRequest;
import yunrry.flik.core.domain.model.plan.TravelCourse;
import yunrry.flik.core.service.plan.CreateTravelCourseService;
import yunrry.flik.ports.in.query.CourseQuery;
import yunrry.flik.ports.in.usecase.TravelCourseUseCase;

import java.util.List;

@Tag(name = "Travel Course", description = "여행 코스 관련 API")
@RestController
@RequestMapping("/v1/travel-courses")
@RequiredArgsConstructor
@Slf4j
public class TravelCourseController {

    private final CreateTravelCourseService createTravelCourseService;
    private final TravelCourseUseCase travelCourseService;

    /**
     * 개인 맞춤 여행 코스 생성
     */
    @Operation(summary = "개인 맞춤 여행 코스 생성", description = "사용자 ID, 선택 카테고리, 여행 기간 등을 기반으로 개인화된 여행 코스를 생성합니다.")
    @PostMapping("/generate")
    public Mono<ResponseEntity<Response<TravelCourseResponse>>> generateTravelCourse(
            @Parameter(description = "카테고리 목록", example = "restaurant,nature,accommodation,indoor,history_culture,cafe,activity,festival,market,themepark")
            @RequestParam @NotEmpty(message = "카테고리는 최소 1개 이상 선택해야 합니다") List<String> categories,

            @Parameter(description = "지역 코드", example = "11100")
            @RequestParam @NotBlank(message = "지역 코드는 필수입니다") String regionCode,

            @Parameter(description = "여행기간", example = "3")
            @RequestParam @Max(value = 3, message = "여행 기간은 최대 3일입니다")
            @Min(value = 1, message = "여행 기간은 최소 1일입니다") int tripDuration,

            @AuthenticationPrincipal Long userId
    ) {
        if (userId == null) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(Response.error("USER_NOT_AUTHENTICATED")));
        }

        CourseQuery query = CourseQuery.of(userId, categories, regionCode, tripDuration);

        return createTravelCourseService.create(query)
                .map(TravelCourseResponse::from)
                .map(response -> ResponseEntity.ok(Response.success(response)))
                .onErrorResume(ex -> {
                    log.error("Error generating travel course for user: {}", userId, ex);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Response.error("COURSE_GENERATION_FAILED")));
                });
    }

    /**
     * 여행 코스 업데이트
     */
    @PutMapping("/{id}")
    public ResponseEntity<Response<TravelCourseResponse>> updateTravelCourse(
            @PathVariable Long id,
            @RequestBody TravelCourseUpdateRequest request
    ) {
        TravelCourse updatedCourse = travelCourseService.updateTravelCourse(id, request);
        return ResponseEntity.ok(Response.success(TravelCourseResponse.from(updatedCourse)));
    }

    /**
     * 사용자가 저장한 모든 여행 코스 조회
     */
    @GetMapping
    public ResponseEntity<Response<List<TravelCourseResponse>>> getTravelCourses(
            @AuthenticationPrincipal Long userId
    ) {
        List<TravelCourse> courses = travelCourseService.getTravelCoursesByUserId(userId);
        List<TravelCourseResponse> response = courses.stream()
                .map(TravelCourseResponse::from)
                .toList();

        return ResponseEntity.ok(Response.success(response));
    }

    /**
     * 특정 여행 코스 단일 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<Response<TravelCourseResponse>> getTravelCourseById(
            @PathVariable Long id
    ) {
        TravelCourse course = travelCourseService.getTravelCourse(id);
        return ResponseEntity.ok(Response.success(TravelCourseResponse.from(course)));
    }

    /**
     * 여행 코스 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deleteTravelCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        TravelCourse course = travelCourseService.getTravelCourse(id);

        if (!course.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.error("FORBIDDEN"));
        }

        travelCourseService.deleteTravelCourse(id);
        return ResponseEntity.ok(Response.success(null));
    }
}
