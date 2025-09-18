package yunrry.flik.core.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import yunrry.flik.core.domain.mapper.CategoryMapper;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.core.service.spot.GetSpotService;
import yunrry.flik.ports.in.query.GetSpotQuery;
import yunrry.flik.ports.in.usecase.UserPreferenceUseCase;
import yunrry.flik.ports.out.repository.UserCategoryPreferenceRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserPreferenceService implements UserPreferenceUseCase {

    private final GetSpotService spotService;
    private final CategoryMapper categoryMapper;
    private final UserCategoryPreferenceRepository userCategoryPreferenceRepository;

    @Override
    @Transactional
    public void updateUserPreferenceFromSavedSpot(Long userId, Long spotId) {
        try {

            GetSpotQuery query = new GetSpotQuery(spotId);
            // 장소 정보 조회
            Spot spot = spotService.getSpot(query);

            String detailCategory = spot.getLabelDepth3();

            // 선호도 점수 증가 (저장 시 +1.0)
            updateCategoryPreference(userId, detailCategory, 1.0);

            log.info("Updated user preference - userId: {}, spotId: {}, category: {}",
                    userId, spotId, detailCategory);
        } catch (Exception e) {
            log.error("Failed to update user preference from saved spot - userId: {}, spotId: {}",
                    userId, spotId, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void updateCategoryPreference(Long userId, String detailCategory, Double increment) {
        userCategoryPreferenceRepository.incrementPreferenceScore(userId, detailCategory, increment);
        log.debug("Updated category preference - userId: {}, category: {}, increment: {}",
                userId, detailCategory, increment);
    }

    @Override
    public int getUserMainCategoryCount(Long userId, String mainCategory) {
        Integer count = userCategoryPreferenceRepository.sumSaveCountByUserIdAndMainCategory(userId, mainCategory);
        return count != null ? count : 0;
    }

}