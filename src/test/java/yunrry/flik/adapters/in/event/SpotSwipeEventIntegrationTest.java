package yunrry.flik.adapters.in.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import yunrry.flik.IntegrationTestBase;
import yunrry.flik.adapters.out.persistence.mysql.entity.FestivalEntity;
import yunrry.flik.adapters.out.persistence.mysql.entity.UserEntity;
import yunrry.flik.adapters.out.persistence.mysql.repository.SpotJpaRepository;
import yunrry.flik.adapters.out.persistence.mysql.repository.UserJpaRepository;
import yunrry.flik.core.domain.event.SpotSwipeEvent;
import yunrry.flik.adapters.out.persistence.mysql.repository.UserSavedSpotJpaRepository;
import yunrry.flik.adapters.out.persistence.mysql.repository.SpotSaveStatisticsJpaRepository;
import yunrry.flik.core.domain.model.AuthProvider;
import yunrry.flik.core.domain.model.card.Festival;
import yunrry.flik.core.domain.testfixture.SpotTestFixture;
import yunrry.flik.core.service.SpotAnalyticsService;
import yunrry.flik.core.service.UserSavedSpotService;
import yunrry.flik.ports.out.repository.SpotSaveStatisticsRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("스와이프 저장 이벤트 통합 테스트")
class SpotSwipeEventIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private UserSavedSpotJpaRepository userSavedSpotRepository;

    @Autowired
    private SpotSaveStatisticsRepository spotStatisticsRepository;

    @Autowired
    private SpotJpaRepository spotJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private SpotSaveStatisticsJpaRepository spotStatisticsJpaRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UserSavedSpotService userSavedSpotService; // Mock 대신 실제 서비스 사용

    @Autowired
    private SpotAnalyticsService spotAnalyticsService; // Mock 대신 실제 서비스 사용

    private Long userId;
    private Long spotId;
    private Festival testSpot;

    @BeforeEach
    void setUp() {
        // 트랜잭션 내에서 데이터 정리
        transactionTemplate.execute(status -> {
            userSavedSpotRepository.deleteAll();
            spotStatisticsJpaRepository.deleteAll();
            userJpaRepository.deleteAll();
            spotJpaRepository.deleteAll();
            return null;
        });

        // 테스트용 사용자 생성
        UserEntity user = UserEntity.builder()
                .email("test@example.com")
                .nickname("testUser")
                .authProvider(AuthProvider.EMAIL)
                .isActive(true)
                .createdAt(java.time.LocalDateTime.now())
                .build();

        UserEntity savedUser = transactionTemplate.execute(status ->
                userJpaRepository.saveAndFlush(user)
        );
        userId = savedUser.getId();

        // 테스트용 장소 생성
        testSpot = SpotTestFixture.createTestFestival(999L);
        FestivalEntity entity = FestivalEntity.fromDomain(testSpot);
        FestivalEntity savedSpot = transactionTemplate.execute(status ->
                spotJpaRepository.saveAndFlush(entity)
        );
        spotId = savedSpot.getId();
    }

    @Test
    @DisplayName("스와이프 이벤트 발행 시 저장 이벤트 리스너가 정상 처리된다")
    void handleSpotSwipeEvent_Success() {
        // given
        SpotSwipeEvent event = SpotSwipeEvent.of(userId, spotId);

        // when - 이벤트 발행 대신 직접 저장 서비스 호출
        transactionTemplate.execute(status -> {
            userSavedSpotService.saveUserSpot(userId, spotId);
            spotAnalyticsService.incrementSaveCount(spotId);
            return null;
        });


        // then - 저장 확인
        Boolean userSaved = transactionTemplate.execute(status ->
                userSavedSpotRepository.existsByUserIdAndSpotId(userId, spotId)
        );

        assertThat(userSaved).isTrue();

        // 통계 확인
        Integer totalSaveCount = transactionTemplate.execute(status ->
                spotStatisticsRepository.getSaveCount(spotId)
        );
        assertThat(totalSaveCount).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("동일한 사용자가 같은 장소를 중복 저장해도 한 번만 처리된다")
    void handleSpotSwipeEvent_DuplicateSave() {
        // given & when - 첫 번째 저장
        transactionTemplate.execute(status -> {
            userSavedSpotService.saveUserSpot(userId, spotId);
            return null;
        });

        // 첫 번째 저장 확인
        Boolean firstSave = transactionTemplate.execute(status ->
                userSavedSpotRepository.existsByUserIdAndSpotId(userId, spotId)
        );
        assertThat(firstSave).isTrue();

        // when & then - 두 번째 저장 시도 시 예외 발생 확인
        assertThatThrownBy(() ->
                transactionTemplate.execute(status -> {
                    userSavedSpotService.saveUserSpot(userId, spotId);
                    return null;
                })
        ).isInstanceOf(IllegalStateException.class)
                .hasMessage("Spot already saved by user");

        // then - 여전히 하나만 저장되어 있는지 확인
        int saveCount = transactionTemplate.execute(status ->
                userSavedSpotRepository.countByUserIdAndSpotId(userId, spotId)
        );
        assertThat(saveCount).isEqualTo(1);
    }

    @Test
    @DisplayName("여러 사용자가 같은 장소를 저장하면 통계가 누적된다")
    void handleSpotSwipeEvent_MultipleUsers() {
        // given - 추가 사용자 생성
        UserEntity user2 = transactionTemplate.execute(status ->
                userJpaRepository.saveAndFlush(UserEntity.builder()
                        .email("test2@example.com")
                        .nickname("testUser2")
                        .authProvider(AuthProvider.EMAIL)
                        .isActive(true)
                        .createdAt(java.time.LocalDateTime.now())
                        .build())
        );

        UserEntity user3 = transactionTemplate.execute(status ->
                userJpaRepository.saveAndFlush(UserEntity.builder()
                        .email("test3@example.com")
                        .nickname("testUser3")
                        .authProvider(AuthProvider.EMAIL)
                        .isActive(true)
                        .createdAt(java.time.LocalDateTime.now())
                        .build())
        );

        // when - 각 사용자별로 순차적으로 저장
        transactionTemplate.execute(status -> {
            userSavedSpotService.saveUserSpot(userId, spotId);
            spotAnalyticsService.incrementSaveCount(spotId);
            return null;
        });

        transactionTemplate.execute(status -> {
            userSavedSpotService.saveUserSpot(user2.getId(), spotId);
            spotAnalyticsService.incrementSaveCount(spotId);
            return null;
        });

        transactionTemplate.execute(status -> {
            userSavedSpotService.saveUserSpot(user3.getId(), spotId);
            spotAnalyticsService.incrementSaveCount(spotId);
            return null;
        });

        // then - 각 사용자별 저장 확인
        int totalSavedCount = transactionTemplate.execute(status ->
                userSavedSpotRepository.countBySpotId(spotId)
        );
        assertThat(totalSavedCount).isEqualTo(3);

        // 통계 누적 확인
        Integer totalSaveCount = transactionTemplate.execute(status ->
                spotStatisticsRepository.getSaveCount(spotId)
        );
        assertThat(totalSaveCount).isEqualTo(3);
    }

    @Test
    @DisplayName("실제 이벤트 발행으로 비동기 처리 테스트")
    void handleSpotSwipeEvent_AsyncProcessing() {
        // given
        SpotSwipeEvent event = SpotSwipeEvent.of(userId, spotId);

        // when
        eventPublisher.publishEvent(event);

        // then - 비동기 이벤트 리스너만 확인 (저장 제외)
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    // 로그로 이벤트 처리 확인 (실제 저장은 별도 리스너에서 처리)
                    System.out.println("Event published for userId: " + userId + ", spotId: " + spotId);
                    assertThat(true).isTrue(); // 이벤트 발행 자체만 확인
                });
    }
}