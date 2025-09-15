package yunrry.flik.ports.in.query;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchSpotsQuery {
    private final int page;
    private final int size;
    private final String category;
    private final String sort;
    private final String keyword;
    private final String address;

    public SearchSpotsQuery(int page, int size, String category, String sort, String keyword, String address) {
        this.page = Math.max(page, 0);
        this.size = (size <= 0 || size > 50) ? 20 : size;
        this.category = category;
        this.sort = sort != null ? sort : "rating";
        this.keyword = validateSearchText(keyword, "검색어");
        this.address = validateSearchText(address, "주소");
    }

    private String validateSearchText(String text, String fieldName) {
        if (text != null && text.trim() != "" &&text.trim().length() < 2) {
            throw new IllegalArgumentException(fieldName + "는 2글자 이상이어야 합니다.");
        }
        return text;
    }

    // 캐시 키 생성 메서드 추가
    public String toCacheKey() {
        return String.join("_",
                String.valueOf(page),
                String.valueOf(size),
                category != null ? category : "",
                sort != null ? sort : "",
                keyword != null ? keyword : "",
                address != null ? address : ""
        );
    }
}
