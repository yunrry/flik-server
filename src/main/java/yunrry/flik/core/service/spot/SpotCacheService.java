package yunrry.flik.core.service.spot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yunrry.flik.core.domain.mapper.CategoryMapper;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.ports.out.repository.SpotRepository;
import yunrry.flik.ports.out.repository.UserSavedSpotRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotCacheService {

    private final UserSavedSpotRepository userSavedSpotRepository;
    private final CategoryMapper categoryMapper;
    private final SpotRepository spotRepository;

    @Cacheable(
            value = "categorySpots",
            key = "#userId + ':' + #category.code + ':' + #regionCode",
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public List<Long> getCategorySpots(Long userId, MainCategory category, String regionCode) {
        log.debug("Cache miss - Loading spots for userId: {}, category: {}, region: {}",
                userId, category.getCode(), regionCode);

        List<String> subCategories = categoryMapper.getSubCategoryNames(category);
        List<Long> userSavedSpotIds = userSavedSpotRepository.findSpotIdsByUserId(userId);

        List<Long> spotIds = spotRepository.findIdsByIdsAndLabelDepth2InAndRegnCd(
                userSavedSpotIds, subCategories, regionCode);

        log.debug("Loaded {} spots for category: {}", spotIds.size(), category.getCode());
        return spotIds;
    }
}