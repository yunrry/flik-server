package yunrry.flik.core.service.spot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.core.domain.testfixture.SpotTestFixture;
import yunrry.flik.ports.out.repository.SpotRepository;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UpdateSpotService 테스트")
class UpdateSpotServiceTest {

    @Mock
    private SpotRepository spotRepository;

    @InjectMocks
    private UpdateSpotService updateSpotService;

    private Spot testSpot;

    @BeforeEach
    void setUp() {
        testSpot = SpotTestFixture.createTestRestaurant(1L);
    }

    @Test
    @DisplayName("ID로 Spot 조회 성공")
    void findById_Success() {
        // Given
        when(spotRepository.findByIdAsync(1L)).thenReturn(Mono.just(testSpot));

        // When & Then
        StepVerifier.create(updateSpotService.findById(1L))
                .expectNext(testSpot)
                .verifyComplete();

        verify(spotRepository).findByIdAsync(1L);
    }

    @Test
    @DisplayName("ID로 Spot 조회 실패")
    void findById_NotFound() {
        // Given
        when(spotRepository.findByIdAsync(1L)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(updateSpotService.findById(1L))
                .verifyComplete();

        verify(spotRepository).findByIdAsync(1L);
    }

    @Test
    @DisplayName("Spot 태그 업데이트 성공 - 3개 키워드")
    void updateSpotTags_ThreeKeywords_Success() {
        // Given
        List<String> keywords = Arrays.asList("카페", "브런치", "데이트");

        when(spotRepository.findById(1L)).thenReturn(testSpot);
        doNothing().when(spotRepository).save(any(Spot.class));

        // When & Then
        StepVerifier.create(updateSpotService.updateSpotTags(1L, keywords))
                .verifyComplete();

        verify(spotRepository).findById(1L);
        verify(spotRepository).save(argThat(spot ->
                "카페".equals(spot.getTag1()) &&
                        "브런치".equals(spot.getTag2()) &&
                        "데이트".equals(spot.getTag3()) &&
                        spot.getTags() == null
        ));
    }

    @Test
    @DisplayName("Spot 태그 업데이트 성공 - 5개 키워드")
    void updateSpotTags_FiveKeywords_Success() {
        // Given
        List<String> keywords = Arrays.asList("카페", "브런치", "데이트", "맛집", "분위기");

        when(spotRepository.findById(1L)).thenReturn(testSpot);
        doNothing().when(spotRepository).save(any(Spot.class));

        // When & Then
        StepVerifier.create(updateSpotService.updateSpotTags(1L, keywords))
                .verifyComplete();

        verify(spotRepository).findById(1L);
        verify(spotRepository).save(argThat(spot ->
                "카페".equals(spot.getTag1()) &&
                        "브런치".equals(spot.getTag2()) &&
                        "데이트".equals(spot.getTag3()) &&
                        "맛집,분위기".equals(spot.getTags())
        ));
    }

    @Test
    @DisplayName("Spot 태그 업데이트 성공 - 1개 키워드")
    void updateSpotTags_OneKeyword_Success() {
        // Given
        List<String> keywords = Arrays.asList("카페");

        when(spotRepository.findById(1L)).thenReturn(testSpot);
        doNothing().when(spotRepository).save(any(Spot.class));

        // When & Then
        StepVerifier.create(updateSpotService.updateSpotTags(1L, keywords))
                .verifyComplete();

        verify(spotRepository).findById(1L);
        verify(spotRepository).save(argThat(spot ->
                "카페".equals(spot.getTag1()) &&
                        spot.getTag2() == null &&
                        spot.getTag3() == null &&
                        spot.getTags() == null
        ));
    }

    @Test
    @DisplayName("Spot 태그 업데이트 성공 - 빈 키워드 리스트")
    void updateSpotTags_EmptyKeywords_Success() {
        // Given
        List<String> keywords = Arrays.asList();

        when(spotRepository.findById(1L)).thenReturn(testSpot);
        doNothing().when(spotRepository).save(any(Spot.class));

        // When & Then
        StepVerifier.create(updateSpotService.updateSpotTags(1L, keywords))
                .verifyComplete();

        verify(spotRepository).findById(1L);
        verify(spotRepository).save(argThat(spot ->
                spot.getTag1() == null &&
                        spot.getTag2() == null &&
                        spot.getTag3() == null &&
                        spot.getTags() == null
        ));
    }

    @Test
    @DisplayName("Spot 태그 업데이트 실패 - Spot 조회 실패")
    void updateSpotTags_SpotNotFound_Failure() {
        // Given
        List<String> keywords = Arrays.asList("카페", "브런치");

        when(spotRepository.findById(1L))
                .thenThrow(new RuntimeException("Spot not found"));

        // When & Then
        StepVerifier.create(updateSpotService.updateSpotTags(1L, keywords))
                .expectError(RuntimeException.class)
                .verify();

        verify(spotRepository).findById(1L);
        verify(spotRepository, never()).save(any());
    }

    @Test
    @DisplayName("Spot 태그 업데이트 실패 - 저장 실패")
    void updateSpotTags_SaveFailure() {
        // Given
        List<String> keywords = Arrays.asList("카페", "브런치");

        when(spotRepository.findById(1L)).thenReturn(testSpot);
        doThrow(new RuntimeException("Save failed")).when(spotRepository).save(any(Spot.class));

        // When & Then
        StepVerifier.create(updateSpotService.updateSpotTags(1L, keywords))
                .expectError(RuntimeException.class)
                .verify();

        verify(spotRepository).findById(1L);
        verify(spotRepository).save(any(Spot.class));
    }

    @Test
    @DisplayName("Spot 태그 업데이트 실패 - 로그 검증")
    void updateSpotTags_LogsErrorOnFailure() {
        // Given
        List<String> keywords = Arrays.asList("카페", "브런치");

        when(spotRepository.findById(1L))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        StepVerifier.create(updateSpotService.updateSpotTags(1L, keywords))
                .expectError(RuntimeException.class)
                .verify();

        verify(spotRepository).findById(1L);
        // 로그는 실제로 출력되지만 테스트에서는 검증하기 어려움
        // 실제 환경에서는 로그 레벨을 확인하거나 로그 캡처 라이브러리 사용
    }


    @Test
    @DisplayName("Spot 태그 업데이트 성공 - 많은 키워드 (10개)")
    void updateSpotTags_ManyKeywords_Success() {
        // Given
        List<String> keywords = Arrays.asList(
                "카페", "브런치", "데이트", "맛집", "분위기",
                "디저트", "커피", "베이커리", "아침식사", "점심"
        );

        when(spotRepository.findById(1L)).thenReturn(testSpot);
        doNothing().when(spotRepository).save(any(Spot.class));

        // When & Then
        StepVerifier.create(updateSpotService.updateSpotTags(1L, keywords))
                .verifyComplete();

        verify(spotRepository).findById(1L);
        verify(spotRepository).save(argThat(spot ->
                "카페".equals(spot.getTag1()) &&
                        "브런치".equals(spot.getTag2()) &&
                        "데이트".equals(spot.getTag3()) &&
                        "맛집,분위기,디저트,커피,베이커리,아침식사,점심".equals(spot.getTags())
        ));
    }
}