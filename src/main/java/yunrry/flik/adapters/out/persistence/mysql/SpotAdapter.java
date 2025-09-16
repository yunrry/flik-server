package yunrry.flik.adapters.out.persistence.mysql;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import yunrry.flik.adapters.out.persistence.mysql.entity.*;
import yunrry.flik.adapters.out.persistence.mysql.repository.SpotJpaRepository;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.card.*;
import yunrry.flik.core.domain.mapper.CategoryMapper;
import yunrry.flik.ports.in.query.SearchSpotsQuery;
import yunrry.flik.ports.out.repository.SpotRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SpotAdapter implements SpotRepository {

    private final SpotJpaRepository spotJpaRepository;
    private final CategoryMapper categoryMappingService;

    @Override
    public Spot findById(Long id) {
        return spotJpaRepository.findById(id)
                .map(entity -> ((BaseSpotEntity) entity).toDomain())
                .orElseThrow(() -> new IllegalArgumentException("Spot not found: " + id));
    }

    @Override
    public Mono<Spot> findByIdAsync(Long id) {
        return Mono.fromCallable(() -> findById(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Slice<Spot> findByConditions(SearchSpotsQuery query) {
        Pageable pageable = createPageable(query);

        Slice<? extends BaseSpotEntity> entities = spotJpaRepository.findByConditions(
                query.getCategory(),
                query.getKeyword(),
                query.getAddress(),
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
        List<String> subcategories = categoryMappingService.getSubCategoryNames(category.getCode());

        System.out.println("Category: " + category + ", subcategories: " + subcategories);
        System.out.println("Original regionCode: " + regionCode);

        String regnCd = regionCode.substring(0, 2);
        String signguCd = regionCode.substring(2, 5);
        System.out.println("regnCd: " + regnCd + ", signguCd: " + signguCd);

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "rating"));

        List<BaseSpotEntity> entities = spotJpaRepository.findByLabelDepth2InAndRegnCdAndSignguCdOrderByRatingDesc(
                subcategories, regnCd, signguCd, pageable);
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
        return spotJpaRepository.findByIdsAndLabelDepth2In(spotIds, labelDepth2Categories).stream()
                .map(BaseSpotEntity::toDomain)
                .collect(Collectors.toList());
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