package yunrry.flik.core.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("음식점 도메인 모델 테스트")
class RestaurantTest {

    @Test
    @DisplayName("음식점 생성 시 필수 정보가 올바르게 설정된다")
    void shouldCreateRestaurantWithRequiredFields() {
        // given & when
        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .name("마리오네")
                .category("이탈리아 음식")
                .rating(BigDecimal.valueOf(4.7))
                .address("서울 성동구 성수동2가 299-50")
                .imageUrls(List.of(
                "https://example.com/marione1.jpg",
                "https://example.com/marione2.jpg",
                "https://example.com/marione3.jpg"
                ))
                .build();

        // then
        assertThat(restaurant.getId()).isEqualTo(1L);
        assertThat(restaurant.getName()).isEqualTo("마리오네");
        assertThat(restaurant.getCategory()).isEqualTo("이탈리아 음식");
        assertThat(restaurant.getRating()).isEqualTo(BigDecimal.valueOf(4.7));
        assertThat(restaurant.getAddress()).isEqualTo("서울 성동구 성수동2가 299-50");
        assertThat(restaurant.getImageUrls()).hasSize(3);
    }

    @Test
    @DisplayName("영업 시간 내에는 영업중으로 판단한다")
    void shouldReturnTrueWhenRestaurantIsOpen() {
        // given
        Restaurant restaurant = Restaurant.builder()
                .name("마리오네")
                .openTime(LocalTime.of(12, 0))
                .closeTime(LocalTime.of(18, 0))
                .dayOff("화")
                .build();

        // when & then
        assertThat(restaurant.isOpenAt(LocalTime.of(14, 0), "월")).isTrue();
        assertThat(restaurant.isOpenAt(LocalTime.of(17, 0), "수")).isTrue();
        assertThat(restaurant.isOpenAt(LocalTime.of(12, 0), "목")).isTrue();
        assertThat(restaurant.isOpenAt(LocalTime.of(18, 0), "금")).isTrue();
    }

    @Test
    @DisplayName("영업 시간 외에는 영업중이 아닌 것으로 판단한다")
    void shouldReturnFalseWhenRestaurantIsClosed() {
        // given
        Restaurant restaurant = Restaurant.builder()
                .name("마리오네")
                .openTime(LocalTime.of(12, 0))
                .closeTime(LocalTime.of(18, 0))
                .dayOff("화")
                .build();

        // when & then
        assertThat(restaurant.isOpenAt(LocalTime.of(11, 59), "월")).isFalse();
        assertThat(restaurant.isOpenAt(LocalTime.of(18, 1), "수")).isFalse();
        assertThat(restaurant.isOpenAt(LocalTime.of(14, 0), "화")).isFalse();
    }

    @Test
    @DisplayName("평점 업데이트가 정상적으로 동작한다")
    void shouldUpdateRatingCorrectly() {
        // given
        Restaurant restaurant = Restaurant.builder()
                .name("마리오네")
                .rating(BigDecimal.valueOf(4.5))
                .build();

        // when
        Restaurant newRestaurant = restaurant.updateRating(BigDecimal.valueOf(4.8));

        // then
        assertThat(newRestaurant.getRating()).isEqualTo(BigDecimal.valueOf(4.8));
    }

    @Test
    @DisplayName("거리 정보 업데이트가 정상적으로 동작한다")
    void shouldUpdateDistanceCorrectly() {
        // given
        Restaurant restaurant = Restaurant.builder()
                .name("마리오네")
                .distance(300)
                .build();

        // when
        Restaurant newRestaurant = restaurant.updateDistance(500);

        // then
        assertThat(newRestaurant.getDistance()).isEqualTo(500);
    }
}