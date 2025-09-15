package yunrry.flik.adapters.in.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import yunrry.flik.core.domain.event.SpotSwipeEvent;
import yunrry.flik.core.service.UserPreferenceService;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationEventListener {

    private final UserPreferenceService userPreferenceService;

    @Async("eventTaskExecutor")
    @EventListener
    public void handleSpotSwipe(SpotSwipeEvent event) {
        try {
            log.info("Updating user preference for user: {}, spot: {}",
                    event.getUserId(), event.getSpotId());

            userPreferenceService.updateUserPreferenceFromSavedSpot(
                    event.getUserId(),
                    event.getSpotId()
            );

            log.info("Successfully updated user preference for user: {}, spot: {}",
                    event.getUserId(), event.getSpotId());
        } catch (Exception e) {
            log.error("Failed to update user preference for user: {}, spot: {} - Error: {}",
                    event.getUserId(), event.getSpotId(), e.getMessage(), e);
        }
    }
}