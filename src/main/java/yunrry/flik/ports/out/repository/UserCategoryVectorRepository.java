package yunrry.flik.ports.out.repository;

import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import yunrry.flik.adapters.out.persistence.postgres.UserCategoryVectorAdapter;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.UserCategoryVector;
import yunrry.flik.core.domain.model.embedding.UserVectorStats;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Transactional(transactionManager = "postgresTransactionManager")
public interface UserCategoryVectorRepository {

    void saveUserCategoryVectors(Long userId, Map<MainCategory, List<Double>> categoryVectors);

    Optional<List<Double>> getUserCategoryVector(Long userId, MainCategory category);

    List<Double> getDefaultCategoryVector(MainCategory category);

    void deleteUserCategoryVectors(Long userId);

    Optional<UserCategoryVector> findByUserIdAndCategory(Long userId, MainCategory category);

    List<UserCategoryVector> findByUserId(Long userId);

    UserCategoryVector save(UserCategoryVector userCategoryVector);

    void updateUserPreferenceVector(Long userId, MainCategory category, List<Long> newFavoriteSpotIds);

    void recalculateCategoryVector(Long userId, MainCategory category, List<Long> SpotIds);

    UserVectorStats getUserVectorStats(Long userId);

    boolean existsByUserIdAndCategory(Long userId, MainCategory category);

    void saveUserCategoryVector(Long userId, MainCategory category, List<Double> vector);
}