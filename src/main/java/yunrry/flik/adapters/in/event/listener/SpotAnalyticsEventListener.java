package yunrry.flik.adapters.in.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import yunrry.flik.core.domain.event.SpotSwipeEvent;
import yunrry.flik.core.service.spot.SpotAnalyticsService;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpotAnalyticsEventListener {

    private final SpotAnalyticsService spotAnalyticsService;

    @Async("eventTaskExecutor")
    @EventListener
    public void handleSpotSwipe(SpotSwipeEvent event) {
        try {
            log.info("Recording analytics for spot: {}, saved by user: {}",
                    event.getSpotId(), event.getUserId());

            spotAnalyticsService.incrementSaveCount(event.getSpotId());

            log.debug("Successfully recorded analytics for spot: {}", event.getSpotId());
        } catch (Exception e) {
            log.error("Failed to record analytics for spot: {} - Error: {}",
                    event.getSpotId(), e.getMessage(), e);
        }
    }
}