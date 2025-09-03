package yunrry.flik.ports.in.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yunrry.flik.ports.in.query.SearchRestaurantsQuery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("음식점 검색 쿼리 테스트")
class SearchRestaurantsQueryTest {

    @Test
    @DisplayName("기본 쿼리가 정상적으로 생성된다")
    void shouldCreateQueryWithValidParameters() {
        // given & when
        SearchRestaurantsQuery query = new SearchRestaurantsQuery(0, 20, "이탈리아 음식", "rating", "피자", "성수동");

        // then
        assertThat(query.getPage()).isEqualTo(0);
        assertThat(query.getSize()).isEqualTo(20);
        assertThat(query.getCategory()).isEqualTo("이탈리아 음식");
        assertThat(query.getSort()).isEqualTo("rating");
        assertThat(query.getKeyword()).isEqualTo("피자");
        assertThat(query.getAddress()).isEqualTo("성수동");
    }

    @Test
    @DisplayName("음수 페이지 번호는 0으로 정규화된다")
    void shouldNormalizeNegativePage() {
        // given & when
        SearchRestaurantsQuery query = new SearchRestaurantsQuery(-1, 20, null, null, null, null);

        // then
        assertThat(query.getPage()).isEqualTo(0);
    }

    @Test
    @DisplayName("0 이하의 페이지 크기는 20으로 정규화된다")
    void shouldNormalizeInvalidPageSize() {
        // given & when
        SearchRestaurantsQuery query1 = new SearchRestaurantsQuery(0, 0, null, null, null, null);
        SearchRestaurantsQuery query2 = new SearchRestaurantsQuery(0, -5, null, null, null, null);

        // then
        assertThat(query1.getSize()).isEqualTo(20);
        assertThat(query2.getSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("50을 초과하는 페이지 크기는 20으로 정규화된다")
    void shouldNormalizeOversizedPageSize() {
        // given & when
        SearchRestaurantsQuery query = new SearchRestaurantsQuery(0, 51, null, null, null, null);

        // then
        assertThat(query.getSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("null 정렬 기준은 distance로 기본값 설정된다")
    void shouldSetDefaultSortWhenNull() {
        // given & when
        SearchRestaurantsQuery query = new SearchRestaurantsQuery(0, 20, null, null, null, null);

        // then
        assertThat(query.getSort()).isEqualTo("distance");
    }

    @Test
    @DisplayName("Builder 패턴으로 쿼리를 생성할 수 있다")
    void shouldCreateQueryWithBuilder() {
        // given & when
        SearchRestaurantsQuery query = SearchRestaurantsQuery.builder()
                .page(1)
                .size(10)
                .category("중식")
                .sort("rating")
                .keyword("짜장면")
                .address("강남구")
                .build();

        // then
        assertThat(query.getPage()).isEqualTo(1);
        assertThat(query.getSize()).isEqualTo(10);
        assertThat(query.getCategory()).isEqualTo("중식");
        assertThat(query.getSort()).isEqualTo("rating");
        assertThat(query.getKeyword()).isEqualTo("짜장면");
        assertThat(query.getAddress()).isEqualTo("강남구");
    }

    @Test
    @DisplayName("null 값들이 허용된다")
    void shouldAllowNullValues() {
        // given & when
        SearchRestaurantsQuery query = new SearchRestaurantsQuery(0, 20, null, "distance", null, null);

        // then
        assertThat(query.getCategory()).isNull();
        assertThat(query.getKeyword()).isNull();
        assertThat(query.getAddress()).isNull();
    }

    @Test
    @DisplayName("공백 값들이 허용된다")
    void shouldAllowEmptyValues() {
        // given & when
        SearchRestaurantsQuery query = new SearchRestaurantsQuery(0, 20, "카테고리", "distance", "", "");

        // then
        assertThat(query.getKeyword()).isEmpty();
        assertThat(query.getAddress()).isEmpty();
    }


    @Test
    @DisplayName("키워드가 1글자이면 예외가 발생한다")
    void shouldThrowExceptionWhenKeywordIsBlank() {
        // when & then
        assertThatThrownBy(() -> new SearchRestaurantsQuery(0, 20, null, null, "너", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("검색어는 2글자 이상이어야 합니다.");
    }

    @Test
    @DisplayName("주소가 1글자이면 예외가 발생한다")
    void shouldThrowExceptionWhenAddressIsBlank() {
        // when & then
        assertThatThrownBy(() -> new SearchRestaurantsQuery(0, 20, null, null, null, "아"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주소는 2글자 이상이어야 합니다.");
    }
}