// test/java/yunrry/flik/core/service/SearchRestaurantsServiceTest.java
package yunrry.flik.core.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import yunrry.flik.core.domain.model.Restaurant;
import yunrry.flik.core.service.card.SearchRestaurantsService;
import yunrry.flik.ports.in.query.SearchRestaurantsQuery;
import yunrry.flik.ports.out.repository.RestaurantRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("음식점 검색 서비스 테스트")
class SearchRestaurantsServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private SearchRestaurantsService searchRestaurantsService;

    @Test
    @DisplayName("카테고리별 음식점 검색이 성공한다")
    void shouldSearchRestaurantsByCategory() {
        // given
        SearchRestaurantsQuery query = SearchRestaurantsQuery.builder()
                .page(0)
                .size(20)
                .category("이탈리아 음식")
                .sort("rating")
                .build();

        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .name("마리오네")
                .category("이탈리아 음식")
                .rating(BigDecimal.valueOf(4.7))
                .build();

        Slice<Restaurant> slice = new SliceImpl<>(List.of(restaurant));
        given(restaurantRepository.findByConditions(query)).willReturn(slice);

        // when
        Slice<Restaurant> result = searchRestaurantsService.searchRestaurants(query);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("마리오네");
        assertThat(result.getContent().get(0).getCategory()).isEqualTo("이탈리아 음식");
        then(restaurantRepository).should().findByConditions(query);
    }

    @Test
    @DisplayName("검색어로 음식점을 검색한다")
    void shouldSearchRestaurantsByKeyword() {
        // given
        SearchRestaurantsQuery query = SearchRestaurantsQuery.builder()
                .page(0)
                .size(20)
                .keyword("피자")
                .sort("distance")
                .build();

        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .name("마리오네")
                .description("전통 나폴리 피자")
                .build();

        Slice<Restaurant> slice = new SliceImpl<>(List.of(restaurant));
        given(restaurantRepository.findByConditions(query)).willReturn(slice);

        // when
        Slice<Restaurant> result = searchRestaurantsService.searchRestaurants(query);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDescription()).contains("피자");
    }

    @Test
    @DisplayName("주소로 음식점을 검색한다")
    void shouldSearchRestaurantsByAddress() {
        // given
        SearchRestaurantsQuery query = SearchRestaurantsQuery.builder()
                .page(0)
                .size(20)
                .address("성수동")
                .sort("distance")
                .build();

        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .name("마리오네")
                .address("서울 성동구 성수동2가 299-50")
                .build();

        Slice<Restaurant> slice = new SliceImpl<>(List.of(restaurant));
        given(restaurantRepository.findByConditions(query)).willReturn(slice);

        // when
        Slice<Restaurant> result = searchRestaurantsService.searchRestaurants(query);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAddress()).contains("성수동");
    }



}