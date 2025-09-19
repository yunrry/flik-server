package yunrry.flik.core.service.spot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import yunrry.flik.adapters.in.dto.spot.CategorySpotsResponse;
import yunrry.flik.core.domain.exception.SpotNotFoundException;

import java.util.*;
import java.util.stream.Collectors;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.core.domain.mapper.CategoryMapper;
import yunrry.flik.ports.in.query.FindSpotsByCategoriesSliceQuery;
import yunrry.flik.ports.in.query.GetSpotQuery;
import yunrry.flik.ports.in.usecase.GetSpotUseCase;
import yunrry.flik.ports.in.usecase.SpotUseCase;
import yunrry.flik.ports.out.repository.SpotRepository;
import yunrry.flik.ports.out.repository.UserSavedSpotRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetSpotService implements GetSpotUseCase {

    private final SpotRepository spotRepository;
    private final CategoryMapper categoryMappingService;
    private final UserSavedSpotRepository userSavedSpotRepository;

    @Override
    @Cacheable(value = "spots", key = "#query.spotId")
    public Spot getSpot(GetSpotQuery query) {
        return spotRepository.findById(query.getSpotId());
    }

    @Override
    public List<Spot> findSpotsByIds(List<Long> spotIds) {
        if (spotIds == null || spotIds.isEmpty()) {
            return List.of(); // 빈 리스트 반환
        }
        return spotRepository.findAllByIds(spotIds);
    }

    @Override
    @Cacheable(value = "spotsByCategoriesSlice", key = "#query.regionCode + ':' + T(java.util.Objects).hash(#query.categories) + ':' + #query.pageable.pageNumber")
    public Slice<Spot> findSpotsByCategoriesSlice(FindSpotsByCategoriesSliceQuery query) {
        query.validate();
        List<String> subcategories = getSubcategoryNames(query.getCategories());
        return spotRepository.findSliceByLabelDepth2InAndRegnCdAndSignguCd(subcategories, query.getRegionCode(), query.getPageable());
    }


    @Override
    @Cacheable(value = "spotsByCategories", key = "T(yunrry.flik.core.service.spot.GetSpotService).createCacheKey(#categories, #regionCode, #limitPerCategory, #tripDuration)")
    public List<Spot> findSpotsByCategories(List<MainCategory> categories, String regionCode, int limitPerCategory, int tripDuration, Long userId ) {
        validateCategoryCount(categories);

        // 사용자가 저장한 스팟 ID 조회
        List<Long> savedSpotIds = userSavedSpotRepository.findSpotIdsByUserId(userId);

        Map<MainCategory, List<Spot>> categorySpots = new HashMap<>();

        for (MainCategory category : categories) {
            // 카테고리별 전체 스팟 조회
            List<Spot> spots = spotRepository.findByCategory(category, regionCode, limitPerCategory * 2); // 여분 확보
            // 사용자가 저장한 스팟 제외
            List<Spot> filtered = spots.stream()
                    .filter(spot -> !savedSpotIds.contains(spot.getId()))
                    .limit(limitPerCategory) // limitPerCategory 만큼만 선택
                    .toList();

            log.info("Category: {}, regionCode: {}, found {} spots after filtering saved spots", category, regionCode, filtered.size());
            categorySpots.put(category, filtered);
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
    public List<Spot> getSpotsByCategoriesPaged(
            List<MainCategory> categories,
            String regionCode,
            int limitPerCategory,
            Long userId,
            int pageNumber
    ) {
        // 1. validate
        if (categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("Categories cannot be empty");
        }
        if (pageNumber < 1) pageNumber = 1;

        // 2. 사용자 저장 스팟 조회
        Set<Long> alreadyExcluded = new HashSet<>(userSavedSpotRepository.findSpotIdsByUserId(userId));

        Map<MainCategory, List<Spot>> categorySpots = new HashMap<>();

        // 3. 각 카테고리별 스팟 조회 및 이전 페이지 스킵
        for (MainCategory category : categories) {
            List<Spot> spots = spotRepository.findByCategory(category, regionCode, limitPerCategory * (pageNumber + 1));

            List<Spot> filtered = spots.stream()
                    .filter(s -> !alreadyExcluded.contains(s.getId()))
                    .skip((long) (pageNumber - 1) * limitPerCategory)
                    .limit(limitPerCategory)
                    .toList();

            categorySpots.put(category, filtered);

            if (filtered.size() < limitPerCategory) {
                log.warn("Category {} only has {} spots for page {}, requested {}", category, filtered.size(), pageNumber, limitPerCategory);
            }
        }

        // 4. 균등 분배 (라운드별 3개씩)
        List<Spot> result = new ArrayList<>();
        Map<MainCategory, Integer> indices = new HashMap<>();
        categories.forEach(cat -> indices.put(cat, 0));

        boolean hasMore = true;
        while (hasMore) {
            hasMore = false;
            for (MainCategory category : categories) {
                List<Spot> spots = categorySpots.get(category);
                int index = indices.get(category);

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
    public CategorySpotsResponse findSpotsByCategoriesWithCacheKey(List<MainCategory> categories, String regionCode, int limitPerCategory, int tripDuration, Long userId) {
        String cacheKey = createCacheKey(categories, regionCode, limitPerCategory, tripDuration);
        List<Spot> spots = findSpotsByCategories(categories, regionCode, limitPerCategory, tripDuration, userId);
        return new CategorySpotsResponse(spots, cacheKey);
    }


    @Override
    public List<Spot> getUserSavedSpots(Long userId) {
        List<Long> savedSpotIds = userSavedSpotRepository.findSpotIdsByUserId(userId);
        if (savedSpotIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Spot> spots = spotRepository.findAllByIds(savedSpotIds);
        if (spots.size() != savedSpotIds.size()) {
            Set<Long> foundIds = spots.stream().map(Spot::getId).collect(Collectors.toSet());
            List<Long> missingIds = savedSpotIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            log.warn("Some saved spots not found for user {}: {}", userId, missingIds);
            // 필요시 SpotNotFoundException을 던질 수도 있음
            // throw new SpotNotFoundException("Some saved spots not found: " + missingIds);
        }
        return spots;
    }


    @Override
    public List<Spot> getRandomSpots(int pageNumber, int pageSize) {
        return spotRepository.findRandomSpots(pageNumber, pageSize);
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