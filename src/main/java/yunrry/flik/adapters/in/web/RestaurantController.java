//package yunrry.flik.adapters.in.web;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import io.swagger.v3.oas.annotations.Parameter;
//import org.springframework.data.domain.Slice;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import yunrry.flik.adapters.in.dto.*;
//import yunrry.flik.adapters.in.dto.restaurant.RestaurantDetailResponse;
//import yunrry.flik.adapters.in.dto.restaurant.RestaurantSearchResponse;
//import yunrry.flik.core.domain.model.card.Restaurant;
//import yunrry.flik.ports.in.query.GetRestaurantQuery;
//import yunrry.flik.ports.in.query.SearchRestaurantsQuery;
//import yunrry.flik.ports.in.usecase.RestaurantUseCase;
//import yunrry.flik.ports.in.usecase.SearchRestaurantsUseCase;
//
//@Tag(name = "Restaurant", description = "음식점 API")
//@RestController
//@RequestMapping("/v1/restaurants")
//@RequiredArgsConstructor
//public class RestaurantController {
//
//    private final RestaurantUseCase restaurantUseCase;
//    private final SearchRestaurantsUseCase searchRestaurantsUseCase;
//
//    @Operation(summary = "음식점 상세 조회", description = "음식점 ID로 상세 정보를 조회합니다.")
//    @GetMapping("/{id}")
//    public ResponseEntity<Response<RestaurantDetailResponse>> getRestaurant(@PathVariable Long id) {
//        GetRestaurantQuery query = new GetRestaurantQuery(id);
//        Restaurant restaurant = restaurantUseCase.getRestaurant(query);
//        RestaurantDetailResponse response = RestaurantDetailResponse.from(restaurant);
//        return ResponseEntity.ok(Response.success(response));
//    }
//
//    @Operation(summary = "음식점 검색", description = "조건에 따라 음식점을 검색합니다.")
//    @GetMapping
//    public ResponseEntity<Response<SliceSearchResponse>> searchRestaurants(
//            @Parameter(description = "페이지 번호", example = "0")
//            @RequestParam(defaultValue = "0") int page,
//
//            @Parameter(description = "페이지 크기", example = "20")
//            @RequestParam(defaultValue = "20") int size,
//
//            @Parameter(description = "음식점 카테고리", example = "이탈리아 음식")
//            @RequestParam(required = false) String category,
//
//            @Parameter(description = "정렬 기준 (distance, rating, name)", example = "distance")
//            @RequestParam(defaultValue = "distance") String sort,
//
//            @Parameter(description = "검색어 (음식점명, 메뉴)", example = "피자")
//            @RequestParam(required = false) String keyword,
//
//            @Parameter(description = "주소 검색어", example = "성수동")
//            @RequestParam(required = false) String address) {
//
//        SearchRestaurantsQuery query = new SearchRestaurantsQuery(page, size, category, sort, keyword, address);
//        Slice<Restaurant> restaurants = searchRestaurantsUseCase.searchRestaurants(query);
//        SliceSearchResponse response = RestaurantSearchResponse.from(restaurants);
//
//        return ResponseEntity.ok(Response.success(response));
//    }
//
//}