package yunrry.flik.core.service.card;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yunrry.flik.core.domain.exception.SpotNotFoundException;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.core.domain.testfixture.SpotTestFixture;
import yunrry.flik.core.domain.mapper.CategoryMapper;
import yunrry.flik.ports.in.query.GetSpotQuery;
import yunrry.flik.ports.out.repository.SpotRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetSpotService 테스트")
class GetSpotServiceTest {

    @Mock
    private SpotRepository spotRepository;

    @Mock
    private CategoryMapper categoryMappingService;

    @InjectMocks
    private GetSpotService getSpotService;

    @Test
    @DisplayName("스팟 단건 조회가 성공한다")
    void shouldGetSpotSuccessfully() {
        // given
        Long spotId = 1L;
        Spot expectedSpot = SpotTestFixture.createTestRestaurant();
        GetSpotQuery query = new GetSpotQuery(spotId);

        when(spotRepository.findById(spotId)).thenReturn(Optional.of(expectedSpot));

        // when
        Spot result = getSpotService.getSpot(query);

        // then
        assertThat(result).isEqualTo(expectedSpot);
        assertThat(result.getName()).isEqualTo("테스트 음식점");
    }

    @Test
    @DisplayName("존재하지 않는 스팟 조회시 예외가 발생한다")
    void shouldThrowExceptionWhenSpotNotFound() {
        // given
        Long spotId = 999L;
        GetSpotQuery query = new GetSpotQuery(spotId);

        when(spotRepository.findById(spotId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getSpotService.getSpot(query))
                .isInstanceOf(SpotNotFoundException.class);
    }

    @Test
    @DisplayName("카테고리별 스팟 조회가 균등하게 분배되어 성공한다")
    void shouldFindSpotsByCategoriesWithEvenDistribution() {
        // given
        List<MainCategory> categories = List.of(MainCategory.RESTAURANT, MainCategory.NATURE);
        String regionCode = "11230";
        int limitPerCategory = 6;

        // 카테고리별 테스트 데이터 생성 (각각 6개씩)
        List<Spot> restaurantSpots = List.of(
                SpotTestFixture.createTestRestaurant(),
                SpotTestFixture.createTestRestaurant(),
                SpotTestFixture.createTestRestaurant(),
                SpotTestFixture.createTestRestaurant(),
                SpotTestFixture.createTestRestaurant(),
                SpotTestFixture.createTestRestaurant()
        );

        List<Spot> natureSpots = List.of(
                SpotTestFixture.createTestTourSpot(),
                SpotTestFixture.createTestTourSpot(),
                SpotTestFixture.createTestTourSpot(),
                SpotTestFixture.createTestTourSpot(),
                SpotTestFixture.createTestTourSpot(),
                SpotTestFixture.createTestTourSpot()
        );

        when(spotRepository.findByCategory(MainCategory.RESTAURANT, regionCode, limitPerCategory))
                .thenReturn(restaurantSpots);
        when(spotRepository.findByCategory(MainCategory.NATURE, regionCode, limitPerCategory))
                .thenReturn(natureSpots);

        // when
        List<Spot> result = getSpotService.findSpotsByCategories(categories, regionCode, limitPerCategory);

        // then
        assertThat(result).hasSize(12); // 총 12개 (6 + 6)

        // 첫 번째 라운드: 음식점 3개, 자연 3개
        // 두 번째 라운드: 음식점 3개, 자연 3개
        // 패턴 확인: 음식점 3개 -> 자연 3개 -> 음식점 3개 -> 자연 3개
        // 패턴 확인: 음식점 3개 -> 자연 3개 -> 음식점 3개 -> 자연 3개
        for (int i = 0; i < 3; i++) {
            assertThat(restaurantSpots.get(0).getClass().isInstance(result.get(i))).isTrue();
        }
        for (int i = 3; i < 6; i++) {
            assertThat(natureSpots.get(0).getClass().isInstance(result.get(i))).isTrue();
        }
        for (int i = 6; i < 9; i++) {
            assertThat(restaurantSpots.get(0).getClass().isInstance(result.get(i))).isTrue();
        }
        for (int i = 9; i < 12; i++) {
            assertThat(natureSpots.get(0).getClass().isInstance(result.get(i))).isTrue();
        }
    }

    @Test
    @DisplayName("카테고리별 스팟 개수가 다를 때 올바르게 분배된다")
    void shouldDistributeEvenlyWhenCategoryHasDifferentCount() {
        // given
        List<MainCategory> categories = List.of(MainCategory.RESTAURANT, MainCategory.NATURE);
        String regionCode = "11230";
        int limitPerCategory = 10;

        // 음식점 2개, 자연 7개
        List<Spot> restaurantSpots = List.of(
                SpotTestFixture.createTestRestaurant(),
                SpotTestFixture.createTestRestaurant()
        );

        List<Spot> natureSpots = List.of(
                SpotTestFixture.createTestTourSpot(),
                SpotTestFixture.createTestTourSpot(),
                SpotTestFixture.createTestTourSpot(),
                SpotTestFixture.createTestTourSpot(),
                SpotTestFixture.createTestTourSpot(),
                SpotTestFixture.createTestTourSpot(),
                SpotTestFixture.createTestTourSpot()
        );

        when(spotRepository.findByCategory(MainCategory.RESTAURANT, regionCode, limitPerCategory))
                .thenReturn(restaurantSpots);
        when(spotRepository.findByCategory(MainCategory.NATURE, regionCode, limitPerCategory))
                .thenReturn(natureSpots);

        // when
        List<Spot> result = getSpotService.findSpotsByCategories(categories, regionCode, limitPerCategory);

        // then
        assertThat(result).hasSize(9); // 총 9개 (2 + 7)

        // 첫 번째 라운드: 음식점 2개, 자연 3개 (음식점이 2개뿐이라 2개만)
        // 두 번째 라운드: 자연 3개 (음식점은 더 이상 없음)
        // 세 번째 라운드: 자연 1개 (남은 1개)
        assertThat(result.subList(0, 2)).allMatch(spot -> restaurantSpots.get(0).getClass().isInstance(spot));
        assertThat(result.subList(2, 5)).allMatch(spot -> natureSpots.get(0).getClass().isInstance(spot));
        assertThat(result.subList(5, 8)).allMatch(spot -> natureSpots.get(0).getClass().isInstance(spot));
        assertThat(result.subList(8, 9)).allMatch(spot -> natureSpots.get(0).getClass().isInstance(spot));
    }

    @Test
    @DisplayName("3개 카테고리로 스팟 조회가 성공한다")
    void shouldFindSpotsByThreeCategories() {
        // given
        List<MainCategory> categories = List.of(MainCategory.RESTAURANT, MainCategory.NATURE, MainCategory.ACCOMMODATION);
        String regionCode = "11230";
        int limitPerCategory = 3;

        List<Spot> restaurantSpots = List.of(SpotTestFixture.createTestRestaurant(), SpotTestFixture.createTestRestaurant(), SpotTestFixture.createTestRestaurant());
        List<Spot> natureSpots = List.of(SpotTestFixture.createTestTourSpot(), SpotTestFixture.createTestTourSpot(), SpotTestFixture.createTestTourSpot());
        List<Spot> accommodationSpots = List.of(SpotTestFixture.createTestAccomodation(), SpotTestFixture.createTestAccomodation(), SpotTestFixture.createTestAccomodation());

        when(spotRepository.findByCategory(MainCategory.RESTAURANT, regionCode, limitPerCategory))
                .thenReturn(restaurantSpots);
        when(spotRepository.findByCategory(MainCategory.NATURE, regionCode, limitPerCategory))
                .thenReturn(natureSpots);
        when(spotRepository.findByCategory(MainCategory.ACCOMMODATION, regionCode, limitPerCategory))
                .thenReturn(accommodationSpots);

        // when
        List<Spot> result = getSpotService.findSpotsByCategories(categories, regionCode, limitPerCategory);

        // then
        assertThat(result).hasSize(9); // 총 9개 (3 + 3 + 3)

        // 첫 번째 라운드에서 각 카테고리별로 3개씩 순차적으로 추가
        assertThat(result.subList(0, 3)).allMatch(spot -> restaurantSpots.get(0).getClass().isInstance(spot));
        assertThat(result.subList(3, 6)).allMatch(spot -> natureSpots.get(0).getClass().isInstance(spot));
        assertThat(result.subList(6, 9)).allMatch(spot -> accommodationSpots.get(0).getClass().isInstance(spot));
    }

    @Test
    @DisplayName("카테고리가 1개면 예외가 발생한다")
    void shouldThrowExceptionWhenCategoryCountIsOne() {
        // given
        List<MainCategory> categories = List.of(MainCategory.RESTAURANT);
        String regionCode = "11230";
        int limitPerCategory = 10;

        // when & then
        assertThatThrownBy(() -> getSpotService.findSpotsByCategories(categories, regionCode, limitPerCategory))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("카테고리는 2-4개를 선택해야 합니다.");
    }

    @Test
    @DisplayName("카테고리가 5개면 예외가 발생한다")
    void shouldThrowExceptionWhenCategoryCountIsFive() {
        // given
        List<MainCategory> categories = List.of(
                MainCategory.RESTAURANT,
                MainCategory.NATURE,
                MainCategory.ACCOMMODATION,
                MainCategory.ACTIVITY,
                MainCategory.CAFE
        );
        String regionCode = "11230";
        int limitPerCategory = 10;

        // when & then
        assertThatThrownBy(() -> getSpotService.findSpotsByCategories(categories, regionCode, limitPerCategory))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("카테고리는 2-4개를 선택해야 합니다.");
    }

    @Test
    @DisplayName("빈 카테고리 결과를 처리한다")
    void shouldHandleEmptyCategoryResults() {
        // given
        List<MainCategory> categories = List.of(MainCategory.RESTAURANT, MainCategory.NATURE);
        String regionCode = "99999"; // 존재하지 않는 지역
        int limitPerCategory = 10;

        when(spotRepository.findByCategory(MainCategory.RESTAURANT, regionCode, limitPerCategory))
                .thenReturn(List.of());
        when(spotRepository.findByCategory(MainCategory.NATURE, regionCode, limitPerCategory))
                .thenReturn(List.of());

        // when
        List<Spot> result = getSpotService.findSpotsByCategories(categories, regionCode, limitPerCategory);

        // then
        assertThat(result).isEmpty();
    }
}