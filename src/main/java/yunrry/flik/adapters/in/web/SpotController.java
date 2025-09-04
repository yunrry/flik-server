package yunrry.flik.adapters.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yunrry.flik.adapters.in.dto.*;
import yunrry.flik.adapters.in.dto.spot.SpotDetailResponse;
import yunrry.flik.adapters.in.dto.spot.SpotSearchResponse;
import yunrry.flik.core.domain.model.Spot;
import yunrry.flik.ports.in.query.GetSpotQuery;
import yunrry.flik.ports.in.query.SearchSpotsQuery;
import yunrry.flik.ports.in.usecase.SpotUseCase;
import yunrry.flik.ports.in.usecase.SearchSpotsUseCase;

@Tag(name = "Spot", description = "음식점 API")
@RestController
@RequestMapping("/v1/spots")
@RequiredArgsConstructor
public class SpotController {

    private final SpotUseCase restaurantUseCase;
    private final SearchSpotsUseCase searchSpotsUseCase;

    @Operation(summary = "음식점 상세 조회", description = "음식점 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<Response<SpotDetailResponse>> getSpot(@PathVariable Long id) {
        GetSpotQuery query = new GetSpotQuery(id);
        Spot restaurant = restaurantUseCase.getSpot(query);
        SpotDetailResponse response = SpotDetailResponse.from(restaurant);
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