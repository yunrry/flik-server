package yunrry.flik.adapters.in.event.listener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import yunrry.flik.core.domain.event.SpotSwipeEvent;
import yunrry.flik.core.service.user.UserPreferenceService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationEventListener 테스트")
class RecommendationEventListenerTest {

    @Mock
    private UserPreferenceService userPreferenceService;

    @InjectMocks
    private RecommendationEventListener eventListener;

    @Test
    @DisplayName("사용자 선호도 업데이트 성공")
    void handleSpotSwipe_Success() {
        // given
        Long userId = 1L;
        Long spotId = 100L;
        SpotSwipeEvent event = SpotSwipeEvent.of(userId, spotId);

        // when
        eventListener.handleSpotSwipe(event);

        // then
        verify(userPreferenceService).updateUserPreferenceFromSavedSpot(eq(userId), eq(spotId));
    }

    @Test
    @DisplayName("사용자 선호도 업데이트 중 예외 발생")
    void handleSpotSwipe_Exception() {
        // given
        Long userId = 1L;
        Long spotId = 100L;
        SpotSwipeEvent event = SpotSwipeEvent.of(userId, spotId);

        doThrow(new RuntimeException("Service error"))
                .when(userPreferenceService).updateUserPreferenceFromSavedSpot(userId, spotId);

        // when & then
        eventListener.handleSpotSwipe(event);

        verify(userPreferenceService).updateUserPreferenceFromSavedSpot(eq(userId), eq(spotId));
    }
}