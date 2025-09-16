package yunrry.flik.adapters.in.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import yunrry.flik.core.domain.event.SpotSwipeEvent;
import yunrry.flik.core.service.user.UserSavedSpotService;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSavedSpotEventListener {

    private final UserSavedSpotService userSavedSpotService;

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSpotSwipe(SpotSwipeEvent event) {
        try {
            log.info("Processing spot save for user: {}, spot: {}",
                    event.getUserId(), event.getSpotId());

            userSavedSpotService.saveUserSpot(event.getUserId(), event.getSpotId());

            log.info("Successfully saved spot for user: {}, spot: {}",
                    event.getUserId(), event.getSpotId());
        } catch (IllegalStateException e) {
            log.warn("Spot already saved - user: {}, spot: {}",
                    event.getUserId(), event.getSpotId());
            // 중복 저장은 정상적인 상황이므로 예외를 다시 던지지 않음


    } catch (Exception e) {
            log.error("Failed to save spot for user: {}, spot: {} - Error: {}",
                    event.getUserId(), event.getSpotId(), e.getMessage(), e);
        }
    }
}