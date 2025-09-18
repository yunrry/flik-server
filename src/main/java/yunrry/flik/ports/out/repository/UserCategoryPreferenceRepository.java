package yunrry.flik.ports.out.repository;


import yunrry.flik.adapters.out.persistence.mysql.entity.UserCategoryPreferenceEntity;
import yunrry.flik.core.domain.model.UserCategoryPreference;

import java.util.List;

public interface UserCategoryPreferenceRepository {

    void incrementPreferenceScore(Long userId, String detailCategory, Double increment);

    double getPreferenceScore(Long userId, String detailCategory);

    List<UserCategoryPreference> findByUserIdAndMainCategoryIn(Long userId, List<String> mainCategories);

    Integer sumSaveCountByUserIdAndMainCategory(Long userId, String mainCategory);

    UserCategoryPreference save(UserCategoryPreference userCategoryPreference);
}