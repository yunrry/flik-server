package yunrry.flik.adapters.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yunrry.flik.adapters.in.dto.Response;
import yunrry.flik.adapters.in.dto.SliceSearchResponse;
import yunrry.flik.adapters.in.dto.festival.FestivalDetailResponse;
import yunrry.flik.adapters.in.dto.festival.FestivalSearchResponse;
import yunrry.flik.core.domain.model.Festival;
import yunrry.flik.ports.in.query.GetFestivalQuery;
import yunrry.flik.ports.in.query.SearchFestivalsQuery;
import yunrry.flik.ports.in.usecase.SearchFestivalsUseCase;
import yunrry.flik.ports.in.usecase.FestivalUseCase;

@Tag(name = "Festival", description = "음식점 API")
@RestController
@RequestMapping("/v1/festivals")
@RequiredArgsConstructor
public class FestivalController {

    private final FestivalUseCase festivalUseCase;
    private final SearchFestivalsUseCase searchFestivalsUseCase;

    @Operation(summary = "음식점 상세 조회", description = "음식점 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<Response<FestivalDetailResponse>> getFestival(@PathVariable Long id) {
        GetFestivalQuery query = new GetFestivalQuery(id);
        Festival festival = festivalUseCase.getFestival(query);
        FestivalDetailResponse response = FestivalDetailResponse.from(festival);
        return ResponseEntity.ok(Response.success(response));
    }

    @Operation(summary = "음식점 검색", description = "조건에 따라 음식점을 검색합니다.")
    @GetMapping
    public ResponseEntity<Response<SliceSearchResponse>> searchFestivals(
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

        SearchFestivalsQuery query = new SearchFestivalsQuery(page, size, category, sort, keyword, address);
        Slice<Festival> festivals = searchFestivalsUseCase.searchFestivals(query);
        SliceSearchResponse response = FestivalSearchResponse.from(festivals);

        return ResponseEntity.ok(Response.success(response));
    }

}