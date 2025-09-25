package yunrry.flik.ports.out.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;
import yunrry.flik.adapters.out.persistence.mysql.entity.BaseSpotEntity;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.ports.in.query.SearchSpotsQuery;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SpotRepository {
    Spot findById(Long id);
    List<Spot> findAllByIds(Collection<Long> ids);
    // 비동기
    Mono<Spot> findByIdAsync(Long id);
    Slice<Spot> findByConditions(SearchSpotsQuery query);
    void save(Spot spot);
    // List 형태
    List<Spot> findByLabelDepth2InAndRegnCd(List<String> subcategories, String regionCode);

    // Page 형태 (페이징)
    Page<Spot> findPageByLabelDepth2InAndRegnCdAndSignguCd(List<String> subcategories, String regionCode, Pageable pageable);

    // Slice 형태 (무한스크롤)
    Slice<Spot> findSliceByLabelDepth2InAndRegnCdAndSignguCd(List<String> subcategories, String regionCode, Pageable pageable);


    List<Spot> findByCategory(MainCategory category, String regionCode, int limit);


//    Slice<Spot> findByCategorySlice(MainCategory category, String regionCode, Pageable pageable);

    List<Spot> findByLabelDepth2In(List<String> subcategories);

    List<Spot> findByIdsAndLabelDepth2In(List<Long> spotIds, List<String> labelDepth2Categories);

    List<Long> findIdsByIdsAndLabelDepth2In(List<Long> spotIds, List<String> labelDepth2Categories);

    List<Long> findIdsByIdsAndLabelDepth2InAndRegnCdAndSignguCd(List<Long> spotIds, List<String> labelDepth2Categories, String regionCode);

    List<Long> findIdsByIdsAndLabelDepth2InAndRegnCd(List<Long> spotIds, List<String> labelDepth2Categories, String regionCode);

    List<Spot> findRandomSpots(int pageNumber, int pageSize);
}
