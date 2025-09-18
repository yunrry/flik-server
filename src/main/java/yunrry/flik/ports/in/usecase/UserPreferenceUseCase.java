package yunrry.flik.ports.in.usecase;

import java.math.BigDecimal;

public interface UserPreferenceUseCase {

    /**
     * 저장된 장소를 기반으로 사용자 선호도 업데이트
     */
    void updateUserPreferenceFromSavedSpot(Long userId, Long spotId);

    /**
     * 사용자의 카테고리별 선호도 조회
     */
    void updateCategoryPreference(Long userId, String categoryCode, Double increment);

    int getUserMainCategoryCount(Long userId, String categoryCode);
}