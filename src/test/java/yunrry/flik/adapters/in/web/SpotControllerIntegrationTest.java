package yunrry.flik.adapters.in.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import yunrry.flik.adapters.out.persistence.entity.SpotEntity;
import yunrry.flik.adapters.out.persistence.repository.SpotJpaRepository;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@DisplayName("장소 API 통합 테스트")
class SpotControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpotJpaRepository spotJpaRepository;

    @Test
    @DisplayName("장소 상세 조회 API 성공")
    void shouldGetSpotSuccessfully() throws Exception {
        // given
        SpotEntity spot = SpotEntity.builder()
                .name("성산일출봉")
                .category("자연경관")
                .rating(BigDecimal.valueOf(4.7))
                .description("화산활동으로 만들어진...")
                .address("제주특별자치도 제주시 성산읍")
                .distance(326)
                .openTime(LocalTime.of(12, 0))
                .closeTime(LocalTime.of(18, 0))
                .dayOff("화")
                .imageUrls("https://example.com/1.jpg,https://example.com/2.jpg")
                .build();

        SpotEntity saved = spotJpaRepository.save(spot);

        // when & then
        mockMvc.perform(get("/v1/spots/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(saved.getId()))
                .andExpect(jsonPath("$.data.name").value("성산일출봉"))
                .andExpect(jsonPath("$.data.category").value("자연경관"))
                .andExpect(jsonPath("$.data.rating").value(4.7))
                .andExpect(jsonPath("$.data.distance").value(326))
                .andExpect(jsonPath("$.data.operatingHours").value("12:00 ~ 18:00"))
                .andExpect(jsonPath("$.data.dayOff").value("화"))
                .andExpect(jsonPath("$.data.imageUrls").isArray())
                .andExpect(jsonPath("$.data.imageUrls[0]").value("https://example.com/1.jpg"));
    }

    @Test
    @DisplayName("기본 파라미터로 장소 검색이 성공한다")
    void shouldSearchSpotsWithDefaultParameters() throws Exception {
        // given
        SpotEntity spot = SpotEntity.builder()
                .name("성산일출봉")
                .category("자연경관")
                .rating(BigDecimal.valueOf(4.7))
                .address("제주특별자치도 제주시 성산읍")
                .distance(326)
                .openTime(LocalTime.of(12, 0))
                .closeTime(LocalTime.of(18, 0))
                .dayOff("화")
                .imageUrls("https://example.com/1.jpg")
                .build();

        spotJpaRepository.save(spot);

        // when & then
        mockMvc.perform(get("/v1/spots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("성산일출봉"))
                .andExpect(jsonPath("$.data.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @DisplayName("카테고리 필터로 검색이 성공한다")
    void shouldSearchSpotsByCategory() throws Exception {
        // given
        SpotEntity italianSpot = SpotEntity.builder()
                .name("성산일출봉")
                .category("자연경관")
                .description("화산활동으로 만들어진...")
                .address("제주특별자치도 제주시 성산읍")
                .build();

        SpotEntity chineseSpot = SpotEntity.builder()
                .name("차이나타운")
                .category("중식")
                .description("화산활동으로 만들어진...")
                .address("제주특별자치도 제주시 성산읍")
                .build();

        spotJpaRepository.save(italianSpot);
        spotJpaRepository.save(chineseSpot);

        // when & then
        mockMvc.perform(get("/v1/spots")
                        .param("category", "자연경관")
                        .param("sort", "rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].category").value("자연경관"));
    }

    @Test
    @DisplayName("검색어로 장소 검색이 성공한다")
    void shouldSearchSpotsByKeyword() throws Exception {
        // given
        SpotEntity pizzaSpot = SpotEntity.builder()
                .name("성산일출봉")
                .category("중식")
                .description("화산활동으로 만들어진...")
                .address("제주특별자치도 제주시 성산읍")
                .build();

        SpotEntity pastaSpot = SpotEntity.builder()
                .name("파스타집")
                .category("중식")
                .description("맛있는 파스타")
                .address("제주특별자치도 제주시 성산읍")
                .build();

        spotJpaRepository.save(pizzaSpot);
        spotJpaRepository.save(pastaSpot);

        // when & then
        mockMvc.perform(get("/v1/spots")
                        .param("keyword", "화산활동")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].description").value("화산활동으로 만들어진..."));
    }

    @Test
    @DisplayName("주소로 장소 검색이 성공한다")
    void shouldSearchSpotsByAddress() throws Exception {
        // given
        SpotEntity seongsuSpot = SpotEntity.builder()
                .name("성산일출봉")
                .category("중식")
                .description("화산활동으로 만들어진...")
                .address("제주특별자치도 제주시 성산읍")
                .build();

        SpotEntity gangnamSpot = SpotEntity.builder()
                .name("강남집")
                .category("중식")
                .description("화산활동으로 만들어진...")
                .address("서울 강남구 역삼동 123-45")
                .build();

        spotJpaRepository.save(seongsuSpot);
        spotJpaRepository.save(gangnamSpot);

        // when & then
        mockMvc.perform(get("/v1/spots")
                        .param("address", "성산읍"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].address").value("제주특별자치도 제주시 성산읍"));
    }

    @Test
    @DisplayName("존재하지 않는 장소 조회 시 404 에러")
    void shouldReturn404WhenSpotNotFound() throws Exception {
        mockMvc.perform(get("/v1/spots/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}