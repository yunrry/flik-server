package yunrry.flik.adapters.in.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import yunrry.flik.IntegrationTestBase;
import yunrry.flik.adapters.out.persistence.entity.FestivalEntity;
import yunrry.flik.adapters.out.persistence.repository.FestivalJpaRepository;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@DisplayName("축제 API 통합 테스트")
class FestivalControllerIntegrationTest extends IntegrationTestBase{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FestivalJpaRepository festivalJpaRepository;

    @Test
    @DisplayName("축제 상세 조회 API 성공")
    void shouldGetFestivalSuccessfully() throws Exception {
        // given
        FestivalEntity festival = FestivalEntity.builder()
                .name("방어축제")
                .category("지역축제")
                .rating(BigDecimal.valueOf(4.7))
                .description("자연산 싱싱한 방어회를 맛볼 수 있는..")
                .address("제주특별자치도 서귀포시 대정읍")
                .distance(326)
                .openTime(LocalTime.of(12, 0))
                .closeTime(LocalTime.of(18, 0))
                .dayOff("화")
                .imageUrls("https://example.com/1.jpg,https://example.com/2.jpg")
                .build();

        FestivalEntity saved = festivalJpaRepository.save(festival);

        // when & then
        mockMvc.perform(get("/v1/festivals/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(saved.getId()))
                .andExpect(jsonPath("$.data.name").value("방어축제"))
                .andExpect(jsonPath("$.data.category").value("지역축제"))
                .andExpect(jsonPath("$.data.rating").value(4.7))
                .andExpect(jsonPath("$.data.distance").value(326))
                .andExpect(jsonPath("$.data.operatingHours").value("12:00 ~ 18:00"))
                .andExpect(jsonPath("$.data.dayOff").value("화"))
                .andExpect(jsonPath("$.data.imageUrls").isArray())
                .andExpect(jsonPath("$.data.imageUrls[0]").value("https://example.com/1.jpg"));
    }

    @Test
    @DisplayName("기본 파라미터로 축제 검색이 성공한다")
    void shouldSearchFestivalsWithDefaultParameters() throws Exception {
        // given
        FestivalEntity festival = FestivalEntity.builder()
                .name("방어축제")
                .category("지역축제")
                .rating(BigDecimal.valueOf(4.7))
                .address("제주특별자치도 서귀포시 대정읍")
                .distance(326)
                .openTime(LocalTime.of(12, 0))
                .closeTime(LocalTime.of(18, 0))
                .dayOff("화")
                .imageUrls("https://example.com/1.jpg")
                .build();

        festivalJpaRepository.save(festival);

        // when & then
        mockMvc.perform(get("/v1/festivals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("방어축제"))
                .andExpect(jsonPath("$.data.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @DisplayName("카테고리 필터로 검색이 성공한다")
    void shouldSearchFestivalsByCategory() throws Exception {
        // given
        FestivalEntity italianFestival = FestivalEntity.builder()
                .name("방어축제")
                .category("지역축제")
                .description("자연산 싱싱한 방어회를 맛볼 수 있는..")
                .address("제주특별자치도 서귀포시 대정읍")
                .build();

        FestivalEntity chineseFestival = FestivalEntity.builder()
                .name("차이나타운")
                .category("중식")
                .description("자연산 싱싱한 방어회를 맛볼 수 있는..")
                .address("제주특별자치도 서귀포시 대정읍")
                .build();

        festivalJpaRepository.save(italianFestival);
        festivalJpaRepository.save(chineseFestival);

        // when & then
        mockMvc.perform(get("/v1/festivals")
                        .param("category", "지역축제")
                        .param("sort", "rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].category").value("지역축제"));
    }

    @Test
    @DisplayName("검색어로 축제 검색이 성공한다")
    void shouldSearchFestivalsByKeyword() throws Exception {
        // given
        FestivalEntity pizzaFestival = FestivalEntity.builder()
                .name("방어축제")
                .category("중식")
                .description("자연산 싱싱한 방어회를 맛볼 수 있는..")
                .address("제주특별자치도 서귀포시 대정읍")
                .build();

        FestivalEntity pastaFestival = FestivalEntity.builder()
                .name("파스타집")
                .category("중식")
                .description("맛있는 파스타")
                .address("제주특별자치도 서귀포시 대정읍")
                .build();

        festivalJpaRepository.save(pizzaFestival);
        festivalJpaRepository.save(pastaFestival);

        // when & then
        mockMvc.perform(get("/v1/festivals")
                        .param("keyword", "방어")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].description").value("자연산 싱싱한 방어회를 맛볼 수 있는.."));
    }

    @Test
    @DisplayName("주소로 축제 검색이 성공한다")
    void shouldSearchFestivalsByAddress() throws Exception {
        // given
        FestivalEntity seongsuFestival = FestivalEntity.builder()
                .name("방어축제")
                .category("중식")
                .description("자연산 싱싱한 방어회를 맛볼 수 있는..")
                .address("제주특별자치도 서귀포시 대정읍")
                .build();

        FestivalEntity gangnamFestival = FestivalEntity.builder()
                .name("강남집")
                .category("중식")
                .description("자연산 싱싱한 방어회를 맛볼 수 있는..")
                .address("서울 강남구 역삼동 123-45")
                .build();

        festivalJpaRepository.save(seongsuFestival);
        festivalJpaRepository.save(gangnamFestival);

        // when & then
        mockMvc.perform(get("/v1/festivals")
                        .param("address", "대정읍"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].address").value("제주특별자치도 서귀포시 대정읍"));
    }

    @Test
    @DisplayName("존재하지 않는 축제 조회 시 404 에러")
    void shouldReturn404WhenFestivalNotFound() throws Exception {
        mockMvc.perform(get("/v1/festivals/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}