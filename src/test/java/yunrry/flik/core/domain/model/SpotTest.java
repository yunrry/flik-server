package yunrry.flik.core.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yunrry.flik.core.domain.exception.SpotRunningTimeNullException;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


@DisplayName("음식점 도메인 모델 테스트")
class SpotTest {

    @Test
    @DisplayName("음식점 생성 시 필수 정보가 올바르게 설정된다")
    void shouldCreateSpotWithRequiredFields() {
        // given & when
        Spot spot = Spot.builder()
                .id(1L)
                .name("성산일출봉")
                .category("자연경관")
                .rating(BigDecimal.valueOf(4.7))
                .address("제주특별자치도 제주시 성산읍")
                .imageUrls(List.of(
                        "https://example.com/marione1.jpg",
                        "https://example.com/marione2.jpg",
                        "https://example.com/marione3.jpg"
                ))
                .build();

        // then
        assertThat(spot.getId()).isEqualTo(1L);
        assertThat(spot.getName()).isEqualTo("성산일출봉");
        assertThat(spot.getCategory()).isEqualTo("자연경관");
        assertThat(spot.getRating()).isEqualTo(BigDecimal.valueOf(4.7));
        assertThat(spot.getAddress()).isEqualTo("제주특별자치도 제주시 성산읍");
        assertThat(spot.getImageUrls()).hasSize(3);
    }

    @Test
    @DisplayName("영업 시간 내에는 영업중으로 판단한다")
    void shouldReturnTrueWhenSpotIsOpen() {
        // given
        Spot spot = Spot.builder()
                .name("성산일출봉")
                .openTime(LocalTime.of(12, 0))
                .closeTime(LocalTime.of(18, 0))
                .dayOff("화")
                .build();

        // when & then
        assertThat(spot.isOpenAt(LocalTime.of(14, 0), "월")).isTrue();
        assertThat(spot.isOpenAt(LocalTime.of(17, 0), "수")).isTrue();
        assertThat(spot.isOpenAt(LocalTime.of(12, 0), "목")).isTrue();
        assertThat(spot.isOpenAt(LocalTime.of(18, 0), "금")).isTrue();
    }

    @Test
    @DisplayName("영업 시간 외에는 영업중이 아닌 것으로 판단한다")
    void shouldReturnFalseWhenspotIsClosed() {
        // given
        Spot spot = Spot.builder()
                .name("성산일출봉")
                .openTime(LocalTime.of(12, 0))
                .closeTime(LocalTime.of(18, 0))
                .dayOff("화")
                .build();

        // when & then
        assertThat(spot.isOpenAt(LocalTime.of(11, 59), "월")).isFalse();
        assertThat(spot.isOpenAt(LocalTime.of(18, 1), "수")).isFalse();
        assertThat(spot.isOpenAt(LocalTime.of(14, 0), "화")).isFalse();
    }

    @Test
    @DisplayName("쉬는날이 없을 경우 영업시간 내에 영업중인 것으로 판단한다")
    void shouldTrueWhenSpotIsOpenAndNoDayOff() {
        // given
        Spot spot = Spot.builder()
                .name("성산일출봉")
                .openTime(LocalTime.of(12, 0))
                .closeTime(LocalTime.of(18, 0))
                .dayOff(null)
                .build();

        // when & then
        assertThat(spot.isOpenAt(LocalTime.of(12, 59), null)).isTrue();
        assertThat(spot.isOpenAt(LocalTime.of(15, 1), null)).isTrue();
        assertThat(spot.isOpenAt(LocalTime.of(14, 0), null)).isTrue();
    }

    @Test
    @DisplayName("영업시간이 없을 경우 예외가 발생한다")
    void shouldThrowExceptionWhenSpotDoesntHaveRunningTime() {
        // given
        Spot spot = Spot.builder()
                .id(1L)
                .name("성산일출봉")
                .openTime(null)
                .closeTime(null)
                .dayOff(null)
                .build();

        // when & then
        assertThatThrownBy(() -> spot.isOpenAt(LocalTime.of(11, 59), null))
                .isInstanceOf(SpotRunningTimeNullException.class);

        assertThatThrownBy(() -> spot.isOpenAt(LocalTime.of(18, 1), null))
                .isInstanceOf(SpotRunningTimeNullException.class);

        assertThatThrownBy(() -> spot.isOpenAt(LocalTime.of(14, 0), null))
                .isInstanceOf(SpotRunningTimeNullException.class);
    }


    @Test
    @DisplayName("평점 업데이트가 정상적으로 동작한다")
    void shouldUpdateRatingCorrectly() {
        // given
        Spot spot = Spot.builder()
                .name("성산일출봉")
                .rating(BigDecimal.valueOf(4.5))
                .build();

        // when
        Spot newSpot = spot.updateRating(BigDecimal.valueOf(4.8));

        // then
        assertThat(newSpot.getRating()).isEqualTo(BigDecimal.valueOf(4.8));
    }

    @Test
    @DisplayName("거리 정보 업데이트가 정상적으로 동작한다")
    void shouldUpdateDistanceCorrectly() {
        // given
        Spot spot = Spot.builder()
                .name("성산일출봉")
                .distance(300)
                .build();

        // when
        Spot newSpot = spot.updateDistance(500);

        // then
        assertThat(newSpot.getDistance()).isEqualTo(500);
    }
}