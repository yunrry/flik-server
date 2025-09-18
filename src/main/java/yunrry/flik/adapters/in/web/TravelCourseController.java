package yunrry.flik.adapters.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import yunrry.flik.adapters.in.dto.Response;
import yunrry.flik.core.domain.model.plan.TravelCourse;
import yunrry.flik.core.service.plan.CreateTravelCourseService;
import yunrry.flik.ports.in.query.CourseQuery;

import java.util.List;

@Tag(name = "Travel Course", description = "여행 코스 관련 API")
@RestController
@RequestMapping("/v1/travel-courses")
@RequiredArgsConstructor
public class TravelCourseController {

    private final CreateTravelCourseService createTravelCourseService;

    @Operation(summary = "개인 맞춤 여행 코스 생성", description = "사용자 ID, 선택 카테고리, 여행 기간 등을 기반으로 개인화된 여행 코스를 생성합니다.")
    @PostMapping("/generate")
    public Mono<ResponseEntity<Response<TravelCourse>>> generateTravelCourse(
            @Parameter(description = "카테고리 목록", example = "restaurant,nature,accommodation, indoor, history_culture, cafe, activity, festival, market, themepark")
            @RequestParam List<String> categories,

            @Parameter(description = "지역 코드", example = "11100")
            @RequestParam String regionCode,

            @Parameter(description = "여행기간", example = "3")
            @RequestParam @Max(3) @Min(1) int tripDuration,

            @AuthenticationPrincipal Long userId
            ) {

        CourseQuery query = CourseQuery.of(userId, categories, regionCode, tripDuration);

        return createTravelCourseService.create(query)
                .map(travelCourse -> ResponseEntity.ok(Response.success(travelCourse)))
                .doOnError(err -> {
                    // 필요시 로깅
                    System.err.println("Error generating travel course: " + err.getMessage());
                });
    }
}
