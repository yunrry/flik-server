package yunrry.flik.ports.out.repository;

import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.UserCategoryVector;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserCategoryVectorRepository {

    void saveUserCategoryVectors(Long userId, Map<MainCategory, String> categoryVectors);

    Optional<String> getUserCategoryVector(Long userId, MainCategory category);

    String getDefaultCategoryVector(MainCategory category);

    void deleteUserCategoryVectors(Long userId);

    Optional<UserCategoryVector> findByUserIdAndCategory(Long userId, MainCategory category);

    List<UserCategoryVector> findByUserId(Long userId);

    UserCategoryVector save(UserCategoryVector userCategoryVector);
}