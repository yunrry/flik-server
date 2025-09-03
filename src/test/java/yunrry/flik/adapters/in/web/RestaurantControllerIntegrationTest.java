package yunrry.flik.adapters.in.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import yunrry.flik.adapters.out.persistence.entity.RestaurantEntity;
import yunrry.flik.adapters.out.persistence.repository.RestaurantJpaRepository;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@DisplayName("음식점 API 통합 테스트")
class RestaurantControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestaurantJpaRepository restaurantJpaRepository;

    @Test
    @DisplayName("음식점 상세 조회 API 성공")
    void shouldGetRestaurantSuccessfully() throws Exception {
        // given
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .name("마리오네")
                .category("이탈리아 음식")
                .rating(BigDecimal.valueOf(4.7))
                .description("세계 챔피언 마리오가 선보이는 전통 나폴리 피자와 파스타를 맛볼 수 있는 곳")
                .address("서울 성동구 성수동2가 299-50")
                .distance(326)
                .openTime(LocalTime.of(12, 0))
                .closeTime(LocalTime.of(18, 0))
                .dayOff("화")
                .imageUrls("https://example.com/1.jpg,https://example.com/2.jpg")
                .build();

        RestaurantEntity saved = restaurantJpaRepository.save(restaurant);

        // when & then
        mockMvc.perform(get("/v1/restaurants/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(saved.getId()))
                .andExpect(jsonPath("$.data.name").value("마리오네"))
                .andExpect(jsonPath("$.data.category").value("이탈리아 음식"))
                .andExpect(jsonPath("$.data.rating").value(4.7))
                .andExpect(jsonPath("$.data.distance").value(326))
                .andExpect(jsonPath("$.data.operatingHours").value("12:00 ~ 18:00"))
                .andExpect(jsonPath("$.data.dayOff").value("화"))
                .andExpect(jsonPath("$.data.imageUrls").isArray())
                .andExpect(jsonPath("$.data.imageUrls[0]").value("https://example.com/1.jpg"));
    }

    @Test
    @DisplayName("기본 파라미터로 음식점 검색이 성공한다")
    void shouldSearchRestaurantsWithDefaultParameters() throws Exception {
        // given
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .name("마리오네")
                .category("이탈리아 음식")
                .rating(BigDecimal.valueOf(4.7))
                .address("서울 성동구 성수동2가 299-50")
                .distance(326)
                .openTime(LocalTime.of(12, 0))
                .closeTime(LocalTime.of(18, 0))
                .dayOff("화")
                .imageUrls("https://example.com/1.jpg")
                .build();

        restaurantJpaRepository.save(restaurant);

        // when & then
        mockMvc.perform(get("/v1/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("마리오네"))
                .andExpect(jsonPath("$.data.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @DisplayName("카테고리 필터로 검색이 성공한다")
    void shouldSearchRestaurantsByCategory() throws Exception {
        // given
        RestaurantEntity italianRestaurant = RestaurantEntity.builder()
                .name("마리오네")
                .category("이탈리아 음식")
                .description("전통 나폴리 피자")
                .address("서울 성동구 성수동2가 299-50")
                .build();

        RestaurantEntity chineseRestaurant = RestaurantEntity.builder()
                .name("차이나타운")
                .category("중식")
                .description("전통 나폴리 피자")
                .address("서울 성동구 성수동2가 299-50")
                .build();

        restaurantJpaRepository.save(italianRestaurant);
        restaurantJpaRepository.save(chineseRestaurant);

        // when & then
        mockMvc.perform(get("/v1/restaurants")
                        .param("category", "이탈리아 음식")
                        .param("sort", "rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].category").value("이탈리아 음식"));
    }

    @Test
    @DisplayName("검색어로 음식점 검색이 성공한다")
    void shouldSearchRestaurantsByKeyword() throws Exception {
        // given
        RestaurantEntity pizzaRestaurant = RestaurantEntity.builder()
                .name("마리오네")
                .category("중식")
                .description("전통 나폴리 피자")
                .address("서울 성동구 성수동2가 299-50")
                .build();

        RestaurantEntity pastaRestaurant = RestaurantEntity.builder()
                .name("파스타집")
                .category("중식")
                .description("맛있는 파스타")
                .address("서울 성동구 성수동2가 299-50")
                .build();

        restaurantJpaRepository.save(pizzaRestaurant);
        restaurantJpaRepository.save(pastaRestaurant);

        // when & then
        mockMvc.perform(get("/v1/restaurants")
                        .param("keyword", "피자")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].description").value("전통 나폴리 피자"));
    }

    @Test
    @DisplayName("주소로 음식점 검색이 성공한다")
    void shouldSearchRestaurantsByAddress() throws Exception {
        // given
        RestaurantEntity seongsuRestaurant = RestaurantEntity.builder()
                .name("마리오네")
                .category("중식")
                .description("전통 나폴리 피자")
                .address("서울 성동구 성수동2가 299-50")
                .build();

        RestaurantEntity gangnamRestaurant = RestaurantEntity.builder()
                .name("강남집")
                .category("중식")
                .description("전통 나폴리 피자")
                .address("서울 강남구 역삼동 123-45")
                .build();

        restaurantJpaRepository.save(seongsuRestaurant);
        restaurantJpaRepository.save(gangnamRestaurant);

        // when & then
        mockMvc.perform(get("/v1/restaurants")
                        .param("address", "성수동"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].address").value("서울 성동구 성수동2가 299-50"));
    }

    @Test
    @DisplayName("존재하지 않는 음식점 조회 시 404 에러")
    void shouldReturn404WhenRestaurantNotFound() throws Exception {
        mockMvc.perform(get("/v1/restaurants/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}