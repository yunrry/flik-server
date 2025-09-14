package yunrry.flik.ports.in.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import yunrry.flik.core.domain.model.MainCategory;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class FindSpotsByCategoriesSliceQuery {
    private final List<MainCategory> categories;
    private final String regionCode;
    private final Pageable pageable;

    public void validate() {
        if (categories == null || categories.size() < 2 || categories.size() > 4) {
            throw new IllegalArgumentException("카테고리는 2-4개를 선택해야 합니다.");
        }
    }
}