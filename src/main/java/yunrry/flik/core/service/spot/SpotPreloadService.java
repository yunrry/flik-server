package yunrry.flik.core.service.spot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.model.MainCategory;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotPreloadService {

    private final SpotCacheService spotCacheService;

    public Map<MainCategory, List<Long>> preloadAllCategorySpots(
            String[][] courseStructure,
            Long userId,
            String regionCode) {

        Set<MainCategory> categories = extractRequiredCategories(courseStructure);
        Map<MainCategory, List<Long>> cache = new HashMap<>();

        log.debug("Preloading spots for categories: {}", categories);

        for (MainCategory category : categories) {
            List<Long> spots = spotCacheService.getCategorySpots(userId, category, regionCode);
            cache.put(category, spots);
        }

        return cache;
    }

    private Set<MainCategory> extractRequiredCategories(String[][] courseStructure) {
        Set<MainCategory> categories = new HashSet<>();
        categories.add(MainCategory.RESTAURANT);
        categories.add(MainCategory.ACCOMMODATION);

        for (String[] day : courseStructure) {
            for (String categoryCode : day) {
                if (categoryCode != null && !categoryCode.isEmpty()) {
                    MainCategory category = MainCategory.findByCode(categoryCode);
                    if (category != null) {
                        categories.add(category);
                    }
                }
            }
        }

        return categories;
    }
}