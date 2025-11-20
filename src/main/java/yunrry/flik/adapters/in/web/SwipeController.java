package yunrry.flik.adapters.in.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import yunrry.flik.adapters.in.dto.Response;
import yunrry.flik.adapters.in.dto.spot.SpotSaveRequest;

import yunrry.flik.adapters.in.dto.swipe.SwipeResponse;
import yunrry.flik.core.service.MetricsService;
import yunrry.flik.core.service.SwipeEventService;
import yunrry.flik.core.service.user.UserSavedSpotService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Slf4j
public class SwipeController {

    private final ApplicationEventPublisher eventPublisher;
    private final UserSavedSpotService userSavedSpotService;
    private final SwipeEventService swipeEventService;
    private final MetricsService metricsService;

    @PostMapping("/swipe")
    public ResponseEntity<Response<SwipeResponse>> swipe(
            @Valid @RequestBody SpotSaveRequest request,
            @AuthenticationPrincipal Long userId) {

        log.info("Swipe request received - userId: {}, spotId: {}", userId, request.getSpotId());

        // 스와이프 메트릭 기록
        metricsService.incrementSwipe();
        // 이벤트 발행
        swipeEventService.publishSwipeEvent(userId, request.getSpotId());

        log.info("SpotSwipeEvent published - userId: {}, spotId: {}", userId, request.getSpotId());
        SwipeResponse response = SwipeResponse.success(request.getSpotId());
        return ResponseEntity.ok(Response.success(response));
    }
}