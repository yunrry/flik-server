package yunrry.flik.adapters.in.event.listener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import yunrry.flik.core.domain.event.SpotSwipeEvent;
import yunrry.flik.core.service.user.UserSavedSpotService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSavedSpotEventListener 테스트")
class UserSavedSpotEventListenerTest {

    @Mock
    private UserSavedSpotService userSavedSpotService;

    @InjectMocks
    private UserSavedSpotEventListener eventListener;

    @Test
    @DisplayName("스와이프 이벤트 처리 성공")
    void handleSpotSwipe_Success() {
        // given
        Long userId = 1L;
        Long spotId = 100L;
        SpotSwipeEvent event = SpotSwipeEvent.of(userId, spotId);

        // when
        eventListener.handleSpotSwipe(event);

        // then
        verify(userSavedSpotService).saveUserSpot(eq(userId), eq(spotId));
    }

    @Test
    @DisplayName("스와이프 이벤트 처리 중 예외 발생")
    void handleSpotSwipe_Exception() {
        // given
        Long userId = 1L;
        Long spotId = 100L;
        SpotSwipeEvent event = SpotSwipeEvent.of(userId, spotId);

        doThrow(new RuntimeException("Database error"))
                .when(userSavedSpotService).saveUserSpot(userId, spotId);

        // when & then
        eventListener.handleSpotSwipe(event);

        verify(userSavedSpotService).saveUserSpot(eq(userId), eq(spotId));
    }
}