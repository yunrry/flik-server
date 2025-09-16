//package yunrry.flik.adapters.in.web;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Mono;
//
//import yunrry.flik.core.service.TravelPlanRecommendationService;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/travel-plans")
//@RequiredArgsConstructor
//@Slf4j
//public class TravelPlanController {
//
//    private final TravelPlanRecommendationService recommendationService;
//
//    @PostMapping("/recommend")
//    public Mono<ResponseEntity<TravelPlanResponse>> recommendTravelPlan(
//            @RequestBody TravelPlanRequest request) {
//
//        log.info("Generating travel plan for user: {}", request.userId());
//
//        return recommendationService.recommendTravelPlan(
//                        request.userId(),
//                        request.locationWeight(),
//                        request.tagWeight(),
//                        request.maxSpots()
//                )
//                .map(spotIds -> ResponseEntity.ok(new TravelPlanResponse(spotIds)))
//                .doOnSuccess(response ->
//                        log.info("Generated travel plan with {} spots", response.getBody().spotIds().size()));
//    }
//
//    public record TravelPlanRequest(
//            Long userId,
//            double locationWeight,
//            double tagWeight,
//            int maxSpots
//    ) {}
//
//    public record TravelPlanResponse(List<Long> spotIds) {}
//}