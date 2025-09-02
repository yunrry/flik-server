package yunrry.flik.adapters.in.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import yunrry.flik.adapters.out.persistence.entity.RestaurantEntity;
import yunrry.flik.adapters.out.persistence.repository.RestaurantJpaRepository;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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
    @DisplayName("존재하지 않는 음식점 조회 시 404 에러")
    void shouldReturn404WhenRestaurantNotFound() throws Exception {
        mockMvc.perform(get("/v1/restaurants/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}