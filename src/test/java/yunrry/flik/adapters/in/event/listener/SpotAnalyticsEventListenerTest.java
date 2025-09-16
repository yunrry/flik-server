package yunrry.flik.adapters.in.event.listener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import yunrry.flik.core.domain.event.SpotSwipeEvent;
import yunrry.flik.core.service.spot.SpotAnalyticsService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpotAnalyticsEventListener 테스트")
class SpotAnalyticsEventListenerTest {

    @Mock
    private SpotAnalyticsService spotAnalyticsService;

    @InjectMocks
    private SpotAnalyticsEventListener eventListener;

    @Test
    @DisplayName("장소 저장 횟수 증가 성공")
    void handleSpotSwipe_Success() {
        // given
        Long userId = 1L;
        Long spotId = 100L;
        SpotSwipeEvent event = SpotSwipeEvent.of(userId, spotId);

        // when
        eventListener.handleSpotSwipe(event);

        // then
        verify(spotAnalyticsService).incrementSaveCount(eq(spotId));
    }

    @Test
    @DisplayName("장소 저장 횟수 증가 중 예외 발생")
    void handleSpotSwipe_Exception() {
        // given
        Long userId = 1L;
        Long spotId = 100L;
        SpotSwipeEvent event = SpotSwipeEvent.of(userId, spotId);

        doThrow(new RuntimeException("Analytics error"))
                .when(spotAnalyticsService).incrementSaveCount(spotId);

        // when & then
        eventListener.handleSpotSwipe(event);

        verify(spotAnalyticsService).incrementSaveCount(eq(spotId));
    }
}