package yunrry.flik.adapters.out.persistence.mysql;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import yunrry.flik.adapters.out.persistence.mysql.entity.*;
import yunrry.flik.adapters.out.persistence.mysql.repository.SigunguCoordinateRepository;
import yunrry.flik.adapters.out.persistence.mysql.repository.SpotJpaRepository;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.card.*;
import yunrry.flik.core.domain.mapper.CategoryMapper;
import yunrry.flik.ports.in.query.SearchSpotsQuery;
import yunrry.flik.ports.out.repository.SpotRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpotAdapter implements SpotRepository {

    private final SpotJpaRepository spotJpaRepository;
    private final CategoryMapper categoryMappingService;
    private final SigunguCoordinateRepository sigunguCoordinateRepository; // 추가 필요

    @Override
    public Spot findById(Long id) {
        return spotJpaRepository.findById(id)
                .map(entity -> ((BaseSpotEntity) entity).toDomain())
                .orElseThrow(() -> new IllegalArgumentException("Spot not found: " + id));
    }

    @Override
    public List<Spot> findAllByIds(Collection<Long> ids) {
        // Iterable<Long>로 JPA에 전달
        Iterable<Long> iterableIds = ids;

        List<BaseSpotEntity> entities = spotJpaRepository.findAllById(iterableIds);

        // 엔티티 -> 도메인 변환
        return entities.stream()
                .map(BaseSpotEntity::toDomain)  // 또는 ((BaseSpotEntity) e).toDomain()
                .toList();
    }

    @Override
    public Mono<Spot> findByIdAsync(Long id) {
        return Mono.fromCallable(() -> findById(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Slice<Spot> findByConditions(SearchSpotsQuery query) {
        Pageable pageable = createPageable(query);
        String categoryStr = query.getCategory() != null ? query.getCategory().toString() : null;

        Slice<? extends BaseSpotEntity> entities = spotJpaRepository.findByKeywordAndFilters(
                query.getKeyword(),
                categoryStr,
                query.getRegionCodePrefix(),
                pageable
        );

        return entities.map(BaseSpotEntity::toDomain);
    }

    @Override
    public void save(Spot spot) {
        BaseSpotEntity entity = switch (spot) {
            case Cultural cultural -> CulturalEntity.fromDomain(cultural);
            case Accommodation accommodation -> AccommodationEntity.fromDomain(accommodation);
            case Restaurant restaurant -> RestaurantEntity.fromDomain(restaurant);
            case TourSpot tourSpot -> TourSpotEntity.fromDomain(tourSpot);
            case Shop shop -> ShopEntity.fromDomain(shop);
            case Festival festival -> FestivalEntity.fromDomain(festival);
            case Leisure leisure -> LeisureEntity.fromDomain(leisure);
            // 다른 타입들 추가
            default -> throw new IllegalArgumentException("Unsupported spot type: " + spot.getClass());
        };
        spotJpaRepository.save(entity);
    }


    @Override
    public List<Spot> findByLabelDepth2InAndRegnCd(List<String> subcategories, String regionCode) {
        String regnCd = regionCode.substring(0, 2);
        String signguCd = regionCode.substring(2, 5);

        List<? extends BaseSpotEntity> entities = spotJpaRepository.findByLabelDepth2InAndRegnCdAndSignguCd(
                subcategories, regnCd, signguCd);
        return entities.stream()
                .map(BaseSpotEntity::toDomain)
                .toList();
    }

    @Override
    public Page<Spot> findPageByLabelDepth2InAndRegnCdAndSignguCd(List<String> subcategories, String regionCode, Pageable pageable) {
        String regnCd = regionCode.substring(0, 2);
        String signguCd = regionCode.substring(2, 5);

        Page<? extends BaseSpotEntity> entities = spotJpaRepository.findPageByLabelDepth2InAndRegnCdAndSignguCd(
                subcategories, regnCd, signguCd, pageable);
        return entities.map(BaseSpotEntity::toDomain);
    }

    @Override
    public Slice<Spot> findSliceByLabelDepth2InAndRegnCdAndSignguCd(List<String> subcategories, String regionCode, Pageable pageable) {
        String regnCd = regionCode.substring(0, 2);
        String signguCd = regionCode.substring(2, 5);

        Slice<? extends BaseSpotEntity> entities = spotJpaRepository.findSliceByLabelDepth2InAndRegnCdAndSignguCd(
                subcategories, regnCd, signguCd, pageable);
        return entities.map(BaseSpotEntity::toDomain);
    }






    @Override
    public List<Spot> findByCategory(MainCategory category, String regionCode, int limit) {
        String regnCd = regionCode.substring(0, 2);
        String signguCd = regionCode.substring(2, 5);

        // 1. 기본 지역에서 검색
        List<Spot> spots = findSpotsByRegion(category, regnCd, signguCd, limit);

        if (spots.size() >= limit) {
            return spots.subList(0, limit);
        }

        log.info("Found {} spots in region {}, need {} more. Expanding search radius...",
                spots.size(), regionCode, limit - spots.size());

        // 2. 반경 확장 검색
        return expandSearchRadius(category, regionCode, spots, limit);
    }

    /**
     * 반경을 점진적으로 확장하여 장소 검색
     */
    private List<Spot> expandSearchRadius(MainCategory category, String regionCode,
                                          List<Spot> initialSpots, int limit) {
        List<Spot> allSpots = new ArrayList<>(initialSpots);
        Set<Long> foundSpotIds = initialSpots.stream()
                .map(Spot::getId)
                .collect(Collectors.toSet());

        // 현재 지역의 좌표 조회
        SigunguCoordinate centerCoordinate = sigunguCoordinateRepository.findBySigCd(regionCode);
        if (centerCoordinate == null) {
            log.warn("No coordinate found for region: {}", regionCode);
            return allSpots;
        }

        double centerX = centerCoordinate.getX();
        double centerY = centerCoordinate.getY();

        // 반경별 검색 (10km, 20km, 30km)
        int[] radiusKm = {10, 20, 30};

        for (int radius : radiusKm) {
            if (allSpots.size() >= limit) break;

            log.info("Searching within {}km radius from {}", radius, regionCode);

            List<Spot> radiusSpots = findSpotsByRadius(category, centerX, centerY,
                    radius, foundSpotIds, limit - allSpots.size());

            for (Spot spot : radiusSpots) {
                if (allSpots.size() >= limit) break;
                if (foundSpotIds.add(spot.getId())) {
                    allSpots.add(spot);
                }
            }

            log.info("After {}km search: {} total spots", radius, allSpots.size());
        }

        return allSpots;
    }

    /**
     * 특정 반경 내의 장소들 검색 (배치 처리로 성능 개선)
     */
    private List<Spot> findSpotsByRadius(MainCategory category, double centerX, double centerY,
                                         int radiusKm, Set<Long> excludeSpotIds, int needed) {
        // 반경 내 시군구 코드들 조회
        List<String> nearbyRegions = sigunguCoordinateRepository.findRegionsWithinRadius(
                centerX, centerY, radiusKm
        );

        if (nearbyRegions.isEmpty()) {
            return new ArrayList<>();
        }

        // 배치로 한 번에 조회 (N+1 문제 해결)
        List<Spot> allSpots = findSpotsByMultipleRegionsBatch(category, nearbyRegions, needed * 2);

        return allSpots.stream()
                .filter(spot -> !excludeSpotIds.contains(spot.getId()))
                .limit(needed)
                .toList();
    }

    /**
     * 여러 지역을 한 번에 조회 (성능 최적화)
     */
    private List<Spot> findSpotsByMultipleRegionsBatch(MainCategory category, List<String> regions, int limit) {
        List<String> regnCds = regions.stream().map(r -> r.substring(0, 2)).distinct().toList();
        List<String> signguCds = regions.stream().map(r -> r.substring(2, 5)).distinct().toList();

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "rating"));

        List<BaseSpotEntity> entities = spotJpaRepository.findByCategoryAndRegnCdInAndSignguCdInOrderByRatingDesc(
                category.toString(), regnCds, signguCds, pageable);

        return entities.stream()
                .map(BaseSpotEntity::toDomain)
                .toList();
    }


    /**
     * 특정 지역에서 장소 검색
     */
    private List<Spot> findSpotsByRegion(MainCategory category, String regnCd, String signguCd, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "rating"));

        List<BaseSpotEntity> entities = spotJpaRepository.findByCategoryAndRegnCdAndSignguCdOrderByRatingDesc(
                category.toString(), regnCd, signguCd, pageable);

        return entities.stream()
                .map(BaseSpotEntity::toDomain)
                .toList();
    }









    @Override
    public List<Spot> findByLabelDepth2In(List<String> subCategories) {
        return spotJpaRepository.findByLabelDepth2In(subCategories).stream()
                .map(BaseSpotEntity::toDomain)
                .collect(Collectors.toList());
    }


    @Override
    public List<Spot> findByIdsAndLabelDepth2In(List<Long> spotIds, List<String> labelDepth2Categories) {
        log.info("Finding spots with IDs: {} and categories: {}", spotIds, labelDepth2Categories);
        return spotJpaRepository.findByIdsAndLabelDepth2In(spotIds, labelDepth2Categories).stream()
                .map(BaseSpotEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> findIdsByIdsAndLabelDepth2In(List<Long> spotIds, List<String> labelDepth2Categories) {
        return spotJpaRepository.findIdsByIdsAndLabelDepth2In(spotIds, labelDepth2Categories);
    };

    @Override
    public List<Long> findIdsByIdsAndLabelDepth2InAndRegnCdAndSignguCd(List<Long> spotIds, List<String> labelDepth2Categories, String regionCode){
        String regnCd = regionCode.substring(0, 2);
        String signguCd = regionCode.substring(2, 5);
        log.info("regnCd: " + regnCd + ", signguCd: " + signguCd);
        return spotJpaRepository.findIdsByIdsAndLabelDepth2InAndRegnCdAndSignguCd(spotIds, labelDepth2Categories, regnCd, signguCd);
    }

    @Override
    public List<Long> findIdsByIdsAndLabelDepth2InAndRegnCd(List<Long> spotIds, List<String> labelDepth2Categories, String regionCode){
        String regnCd = regionCode.substring(0, 2);
        log.info("regnCd: " + regnCd );
        return spotJpaRepository.findIdsByIdsAndLabelDepth2InAndRegnCd(spotIds, labelDepth2Categories, regnCd);
    }


    @Override
    public List<Spot> findRandomSpots(int pageNumber, int pageSize) {
        int offset = (pageNumber - 1) * pageSize;
        List<BaseSpotEntity> entities = spotJpaRepository.findRandomSpots(offset, pageSize);
        return entities.stream()
                .map(BaseSpotEntity::toDomain)
                .toList();
    }

    private Pageable createPageable(SearchSpotsQuery query) {
        Sort sort = createSort(query.getSort());
        return PageRequest.of(query.getPage(), query.getSize(), sort);
    }

    private Sort createSort(String sortBy) {
        return switch (sortBy) {
            case "rating" -> Sort.by(Sort.Direction.DESC, "rating");
            case "name" -> Sort.by(Sort.Direction.ASC, "name");
            default -> Sort.by(Sort.Direction.ASC, "rating");
        };
    }
}