package yunrry.flik.adapters.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
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
            CategorySpotsResponse response = getSpotUseCase.findSpotsByCategoriesWithCacheKey(mainCategories, regionCode, limitPerCategory, tripDuration);



            // 3. 응답 반환
            return ResponseEntity.ok(Response.success(response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Response.error(e.getMessage()));
        }
    }

//    @Operation(summary = "추천 스팟 조회", description = "지역별 추천 스팟들을 조회합니다.")
//    @GetMapping("/recommendations")
//    public ResponseEntity<Response<List<Spot>>> getRecommendedSpots(
//            @Parameter(description = "지역 코드", example = "11")
//            @RequestParam String regionCode,
//
//            @Parameter(description = "개수", example = "5")
//            @RequestParam(defaultValue = "5") int limit) {
//
//        List<Spot> recommendedSpots = getSpotUseCase.getRecommendedSpots(regionCode, limit);
//
//        return ResponseEntity.ok(Response.success(recommendedSpots));
//    }
//




    @Operation(summary = "음식점 상세 조회", description = "음식점 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<Response<SpotDetailResponse>> getSpot(@PathVariable Long id) {
        GetSpotQuery query = new GetSpotQuery(id);
        Spot spot = getSpotUseCase.getSpot(query);
        SpotDetailResponse response = SpotDetailResponse.from(spot);
        return ResponseEntity.ok(Response.success(response));
    }

    @Operation(summary = "음식점 검색", description = "조건에 따라 음식점을 검색합니다.")
    @GetMapping
    public ResponseEntity<Response<SliceSearchResponse>> searchSpots(
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "음식점 카테고리", example = "이탈리아 음식")
            @RequestParam(required = false) String category,

            @Parameter(description = "정렬 기준 (distance, rating, name)", example = "distance")
            @RequestParam(defaultValue = "distance") String sort,

            @Parameter(description = "검색어 (음식점명, 메뉴)", example = "피자")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "주소 검색어", example = "성수동")
            @RequestParam(required = false) String address) {

        SearchSpotsQuery query = new SearchSpotsQuery(page, size, category, sort, keyword, address);
        Slice<Spot> spots = searchSpotsUseCase.searchSpots(query);
        SliceSearchResponse response = SpotSearchResponse.from(spots);

        return ResponseEntity.ok(Response.success(response));
    }

}