package yunrry.flik.ports.out.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.ports.in.query.SearchSpotsQuery;

import java.util.List;
import java.util.Optional;

public interface SpotRepository {
    Optional<Spot> findById(Long id);
    Slice<Spot> findByConditions(SearchSpotsQuery query);
    // List 형태
    List<Spot> findByLabelDepth2InAndRegnCd(List<String> subcategories, String regionCode);

    // Page 형태 (페이징)
    Page<Spot> findPageByLabelDepth2InAndRegnCdAndSignguCd(List<String> subcategories, String regionCode, Pageable pageable);

    // Slice 형태 (무한스크롤)
    Slice<Spot> findSliceByLabelDepth2InAndRegnCdAndSignguCd(List<String> subcategories, String regionCode, Pageable pageable);

    List<Spot> findByCategory(MainCategory category, String regionCode, int limit);

//    Slice<Spot> findByCategorySlice(MainCategory category, String regionCode, Pageable pageable);
}