package yunrry.flik.core.service.card;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import yunrry.flik.adapters.in.dto.spot.CategorySpotsResponse;
import yunrry.flik.core.domain.exception.SpotNotFoundException;
import java.util.stream.Collectors;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.core.domain.mapper.CategoryMapper;
import yunrry.flik.ports.in.query.FindSpotsByCategoriesSliceQuery;
import yunrry.flik.ports.in.query.GetSpotQuery;
import yunrry.flik.ports.in.usecase.SpotUseCase;
import yunrry.flik.ports.out.repository.SpotRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetSpotService implements SpotUseCase {

    private final SpotRepository spotRepository;
    private final CategoryMapper categoryMappingService;

    @Override
    @Cacheable(value = "spots", key = "#query.spotId")
    public Spot getSpot(GetSpotQuery query) {
        return spotRepository.findById(query.getSpotId())
                .orElseThrow(() -> new SpotNotFoundException(query.getSpotId()));
    }


    @Override
    @Cacheable(value = "spotsByCategoriesSlice", key = "#query.regionCode + ':' + T(java.util.Objects).hash(#query.categories) + ':' + #query.pageable.pageNumber")
    public Slice<Spot> findSpotsByCategoriesSlice(FindSpotsByCategoriesSliceQuery query) {
        query.validate();
        List<String> subcategories = getSubcategoryNames(query.getCategories());
        return spotRepository.findSliceByLabelDepth2InAndRegnCdAndSignguCd(subcategories, query.getRegionCode(), query.getPageable());
    }


    @Override
    @Cacheable(value = "spotsByCategories", key = "T(yunrry.flik.core.service.card.GetSpotService).createCacheKey(#categories, #regionCode, #limitPerCategory, #tripDuration)")
    public List<Spot> findSpotsByCategories(List<MainCategory> categories, String regionCode, int limitPerCategory, int tripDuration ) {
        validateCategoryCount(categories);

        Map<MainCategory, List<Spot>> categorySpots = new HashMap<>();

        // 각 카테고리별로 limitPerCategory 개수만큼 spots 수집
        for (MainCategory category : categories) {
            List<Spot> spots = spotRepository.findByCategory(category, regionCode, limitPerCategory);
            log.info("Category: {}, regionCode: {}, found {} spots", category, regionCode, spots.size());
            categorySpots.put(category, spots);
        }

        // 전체 결과 개수 로그
        int totalSpots = categorySpots.values().stream().mapToInt(List::size).sum();
        log.info("Total spots found: {}", totalSpots);

        // 카테고리별로 균등하게 분배하여 결과 생성
        List<Spot> result = new ArrayList<>();
        Map<MainCategory, Integer> indices = new HashMap<>();
        categories.forEach(cat -> indices.put(cat, 0));

        // 각 라운드에서 카테고리당 3개씩 순차적으로 추가
        boolean hasMore = true;
        while (hasMore) {
            hasMore = false;

            for (MainCategory category : categories) {
                List<Spot> spots = categorySpots.get(category);
                int index = indices.get(category);

                // 해당 카테고리에서 3개씩 추가 (남은 개수가 3개 미만이면 그만큼만)
                int itemsToAdd = Math.min(3, spots.size() - index);
                for (int i = 0; i < itemsToAdd; i++) {
                    result.add(spots.get(index + i));
                    hasMore = true;
                }

                indices.put(category, index + itemsToAdd);
            }
        }

        return result;
    }

    @Override
    public CategorySpotsResponse findSpotsByCategoriesWithCacheKey(List<MainCategory> categories, String regionCode, int limitPerCategory, int tripDuration) {
        String cacheKey = createCacheKey(categories, regionCode, limitPerCategory, tripDuration);
        List<Spot> spots = findSpotsByCategories(categories, regionCode, limitPerCategory, tripDuration);
        return new CategorySpotsResponse(spots, cacheKey);
    }



    private List<String> getSubcategoryNames(List<MainCategory> categories) {
        return categories.stream()
                .flatMap(category -> categoryMappingService.getSubCategoryNames(category.getCode()).stream())
                .distinct()
                .toList();
    }

    private void validateCategoryCount(List<MainCategory> categories) {
        long validCategoryCount = categories.stream()
                .filter(category -> category != MainCategory.RESTAURANT && category != MainCategory.ACCOMMODATION)
                .count();
        if (validCategoryCount < 2 || validCategoryCount > 4) {
            throw new IllegalArgumentException("카테고리는 2-4개를 선택해야 합니다.");
        }
    }



    public static String createCacheKey(List<MainCategory> categories, String regionCode, int limitPerCategory, int tripDuration) {
        String sortedCategories = categories.stream()
                .sorted()
                .map(MainCategory::name)
                .collect(Collectors.joining("_"));

        return sortedCategories + ":" + regionCode + ":" + limitPerCategory + ":" + tripDuration;
    }
}