package yunrry.flik.adapters.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yunrry.flik.adapters.in.dto.Response;
import yunrry.flik.adapters.in.dto.RestaurantDetailResponse;
import yunrry.flik.core.domain.model.Restaurant;
import yunrry.flik.ports.in.query.GetRestaurantQuery;
import yunrry.flik.ports.in.usecase.RestaurantUseCase;

@Tag(name = "Restaurant", description = "음식점 API")
@RestController
@RequestMapping("/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantUseCase restaurantUseCase;

    @Operation(summary = "음식점 상세 조회", description = "음식점 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<Response<RestaurantDetailResponse>> getRestaurant(@PathVariable Long id) {
        GetRestaurantQuery query = new GetRestaurantQuery(id);
        Restaurant restaurant = restaurantUseCase.getRestaurant(query);
        RestaurantDetailResponse response = RestaurantDetailResponse.from(restaurant);
        return ResponseEntity.ok(Response.success(response));
    }
}