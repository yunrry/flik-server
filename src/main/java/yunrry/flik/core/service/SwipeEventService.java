package yunrry.flik.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yunrry.flik.core.domain.event.SpotSwipeEvent;

@Service
@RequiredArgsConstructor
public class SwipeEventService {

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void publishSwipeEvent(Long userId, Long spotId) {
        SpotSwipeEvent event = SpotSwipeEvent.of(userId, spotId);
        eventPublisher.publishEvent(event);
    }
}