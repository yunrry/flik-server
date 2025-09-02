package yunrry.flik.core.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yunrry.flik.core.domain.exception.RestaurantNotFoundException;
import yunrry.flik.core.domain.model.Restaurant;
import yunrry.flik.ports.in.usecase.GetRestaurantQuery;
import yunrry.flik.ports.out.repository.RestaurantRepository;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("음식점 조회 서비스 테스트")
class GetRestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private GetRestaurantService getRestaurantService;

    @Test
    @DisplayName("음식점 ID로 상세 정보를 성공적으로 조회한다")
    void shouldGetRestaurantByIdSuccessfully() {
        // given
        Long restaurantId = 1L;
        GetRestaurantQuery query = new GetRestaurantQuery(restaurantId);

        Restaurant restaurant = Restaurant.builder()
                .id(restaurantId)
                .name("마리오네")
                .category("이탈리아 음식")
                .rating(BigDecimal.valueOf(4.7))
                .description("세계 챔피언 마리오가 선보이는 전통 나폴리 피자와 파스타를 맛볼 수 있는 곳")
                .address("서울 성동구 성수동2가 299-50")
                .distance(326)
                .openTime(LocalTime.of(12, 0))
                .closeTime(LocalTime.of(18, 0))
                .dayOff("화")
                .imageUrls(List.of(
                        "https://example.com/marione1.jpg",
                        "https://example.com/marione2.jpg",
                        "https://example.com/marione3.jpg"
                ))
                .build();

        given(restaurantRepository.findById(restaurantId))
                .willReturn(Optional.of(restaurant));

        // when
        Restaurant result = getRestaurantService.getRestaurant(query);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("마리오네");
        assertThat(result.getCategory()).isEqualTo("이탈리아 음식");
        assertThat(result.getRating()).isEqualTo(BigDecimal.valueOf(4.7));
        assertThat(result.getDescription()).isEqualTo("세계 챔피언 마리오가 선보이는 전통 나폴리 피자와 파스타를 맛볼 수 있는 곳");
        assertThat(result.getAddress()).isEqualTo("서울 성동구 성수동2가 299-50");
        assertThat(result.getDistance()).isEqualTo(326);
        assertThat(result.getDayOff()).isEqualTo("화");

        then(restaurantRepository).should().findById(restaurantId);
    }

    @Test
    @DisplayName("존재하지 않는 음식점 ID로 조회 시 예외가 발생한다")
    void shouldThrowExceptionWhenRestaurantNotFound() {
        // given
        Long nonExistentId = 999L;
        GetRestaurantQuery query = new GetRestaurantQuery(nonExistentId);

        given(restaurantRepository.findById(nonExistentId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getRestaurantService.getRestaurant(query))
                .isInstanceOf(RestaurantNotFoundException.class)
                .hasMessage("CORE-음식점을 찾을 수 없습니다");

        then(restaurantRepository).should().findById(nonExistentId);
    }

    @Test
    @DisplayName("영업 시간이 설정된 음식점 정보를 정확히 조회한다")
    void shouldGetRestaurantWithOperatingHours() {
        // given
        Long restaurantId = 1L;
        GetRestaurantQuery query = new GetRestaurantQuery(restaurantId);

        Restaurant restaurant = Restaurant.builder()
                .id(restaurantId)
                .name("마리오네")
                .openTime(LocalTime.of(12, 0))
                .closeTime(LocalTime.of(18, 0))
                .dayOff("화")
                .build();

        given(restaurantRepository.findById(restaurantId))
                .willReturn(Optional.of(restaurant));

        // when
        Restaurant result = getRestaurantService.getRestaurant(query);

        // then
        assertThat(result.getOpenTime()).isEqualTo(LocalTime.of(12, 0));
        assertThat(result.getCloseTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(result.getDayOff()).isEqualTo("화");
        assertThat(result.isOpenAt(LocalTime.of(14, 0), "월")).isTrue();
        assertThat(result.isOpenAt(LocalTime.of(14, 0), "화")).isFalse();
    }
}