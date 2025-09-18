package yunrry.flik.adapters.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yunrry.flik.adapters.in.dto.*;
import yunrry.flik.adapters.in.dto.spot.CategorySpotsResponse;
import yunrry.flik.adapters.in.dto.spot.SpotDetailResponse;
import yunrry.flik.adapters.in.dto.spot.SpotSearchResponse;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.ports.in.query.FindSpotsByCategoriesQuery;
import yunrry.flik.ports.in.query.FindSpotsByCategoryQuery;
import yunrry.flik.ports.in.query.GetSpotQuery;
import yunrry.flik.ports.in.query.SearchSpotsQuery;
import yunrry.flik.ports.in.usecase.GetSpotUseCase;
import yunrry.flik.ports.in.usecase.SpotUseCase;
import yunrry.flik.ports.in.usecase.SearchSpotsUseCase;

import java.util.List;

@Tag(name = "Spot", description = "음식점 API")
@RestController
@RequestMapping("/v1/spots")
@RequiredArgsConstructor
public class SpotController {

    private final GetSpotUseCase getSpotUseCase;
    private final SearchSpotsUseCase searchSpotsUseCase;

    @Operation(summary = "카테고리별 스팟 조회", description = "여러 카테고리의 스팟들을 각 카테고리당 최대 20개씩 조회하여 반환합니다.")
    @GetMapping("/categories")
    public ResponseEntity<Response<CategorySpotsResponse>> getSpotsByCategories(
            @Parameter(description = "카테고리 목록", example = "RESTAURANT,NATURE,ACCOMMODATION, INDOOR, HISTORY_CULTURE, CAFE, ACTIVITY, FESTIVAL, MARKET, THEMEPARK")

            @RequestParam List<String> categories,

            @Parameter(description = "지역 코드", example = "11")
            @RequestParam String regionCode,

            @Parameter(description = "여행기간", example = "3")
            @RequestParam @Max(3) @Min(1) int tripDuration,

            @Parameter(description = "각 카테고리별 조회할 개수", example = "21")
            @RequestParam(defaultValue = "21") int limitPerCategory) {

        try {
            // 1. 문자열을 MainCategory enum으로 변환 - 유효성 검사 추가
            List<MainCategory> mainCategories = categories.stream()
                    .map(category -> {
                        try {
                            return MainCategory.valueOf(category.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException("Invalid category: " + category);
                        }
                    })
                    .toList();


            // 2. 비즈니스 로직 실행
            CategorySpotsResponse response = getSpotUseCase.findSpotsByCategoriesWithCacheKey(mainCategories, regionCode, limitPerCategory, tripDuration, 1L); // TODO: userId 동적으로 변경



            // 3. 응답 반환
            return ResponseEntity.ok(Response.success(response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Response.error(e.getMessage()));
        }
    }


    @Operation(summary = "카테고리별 스팟 조회 (페이지네이션)", description = "여러 카테고리의 스팟들을 각 카테고리당 limitPerCategory 만큼 페이지별로 조회하여 반환합니다. 이미 저장한 스팟은 제외됩니다.")
    @GetMapping("/categories/paged")
    public ResponseEntity<Response<CategorySpotsResponsePaged>> getSpotsByCategoriesPaged(
            @Parameter(description = "카테고리 목록", example = "RESTAURANT,NATURE,ACCOMMODATION,INDOOR,HISTORY_CULTURE,CAFE,ACTIVITY,FESTIVAL,MARKET,THEMEPARK")
            @RequestParam List<String> categories,

            @Parameter(description = "지역 코드", example = "11")
            @RequestParam String regionCode,

            @Parameter(description = "페이지 번호", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "각 카테고리별 조회할 개수", example = "21")
            @RequestParam(defaultValue = "21") int limitPerCategory,

            @AuthenticationPrincipal Long userId // 인증된 사용자 ID
    ) {
        try {
            if (userId == null) {
                return ResponseEntity.badRequest().body(Response.error("USER_NOT_AUTHENTICATED"));
            }

            // 문자열을 MainCategory enum으로 변환
            List<MainCategory> mainCategories = categories.stream()
                    .map(cat -> {
                        try {
                            return MainCategory.valueOf(cat.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException("Invalid category: " + cat);
                        }
                    })
                    .toList();

            // 서비스 호출 (기존 List<Spot> 반환)
            List<Spot> spots = getSpotUseCase.getSpotsByCategoriesPaged(
                    mainCategories,
                    regionCode,
                    limitPerCategory,
                    userId,
                    page
            );

            // 다음 페이지 존재 여부 계산
            boolean hasNext = spots.size() == mainCategories.size() * limitPerCategory;

            // DTO로 변환
            CategorySpotsResponsePaged response = CategorySpotsResponsePaged.builder()
                    .page(page)
                    .pageSize(spots.size())
                    .spots(spots)
                    .hasNext(hasNext)
                    .build();

            return ResponseEntity.ok(Response.success(response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Response.error(e.getMessage()));
        }
    }



        @Operation(
                summary = "ID 리스트로 스팟 조회",
                description = "여러 스팟 ID를 기반으로 상세 정보를 조회합니다."
        )
        @GetMapping("/by-ids")
        public ResponseEntity<Response<List<SpotDetailResponse>>> getSpotsByIds(
                @Parameter(description = "조회할 스팟 ID 리스트", example = "1,2,3")
                @RequestParam List<Long> ids
        ) {
            try {
                if (ids == null || ids.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Response.error("Spot IDs cannot be empty"));
                }

                // 서비스 호출
                List<Spot> spots = getSpotUseCase.findSpotsByIds(ids);

                // DTO 변환
                List<SpotDetailResponse> responseList = spots.stream()
                        .map(SpotDetailResponse::from)
                        .toList();

                return ResponseEntity.ok(Response.success(responseList));

            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Response.error(e.getMessage()));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Response.error("Failed to fetch spots"));
            }
        }


    @Operation(summary = "유저 저장 스팟 조회", description = "user 저장 스팟 목록을 반환")
    @GetMapping("/saved")
    public ResponseEntity<Response<List<SpotDetailResponse>>> getUserSavedSpots(
            @AuthenticationPrincipal Long userId // 인증된 사용자 ID
    ) {
        try {
            if (userId == null) {
                return ResponseEntity.badRequest().body(Response.error("USER_NOT_AUTHENTICATED"));
            }

            List<Spot> savedSpots = getSpotUseCase.getUserSavedSpots(userId);

            List<SpotDetailResponse> responseList = savedSpots.stream()
                    .map(SpotDetailResponse::from)
                    .toList();

            return ResponseEntity.ok(Response.success(responseList));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Response.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to fetch saved spots"));
        }
    }





}