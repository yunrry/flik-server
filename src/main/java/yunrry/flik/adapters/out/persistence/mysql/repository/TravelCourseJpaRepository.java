package yunrry.flik.adapters.out.persistence.mysql.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yunrry.flik.adapters.out.persistence.mysql.entity.TravelCourseEntity;
import yunrry.flik.core.domain.model.plan.CourseType;

import java.time.LocalDateTime;
import java.util.List;

public interface TravelCourseJpaRepository extends JpaRepository<TravelCourseEntity, Long> {

    List<TravelCourseEntity> findByUserId(Long userId);

    List<TravelCourseEntity> findByUserIdAndCourseType(Long userId, CourseType courseType);

    List<TravelCourseEntity> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime createdAt);

    boolean existsByUserIdAndCourseType(Long userId, CourseType courseType);

    @Modifying
    @Query("DELETE FROM TravelCourseEntity t WHERE t.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM TravelCourseEntity t WHERE t.userId = :userId ORDER BY t.createdAt DESC")
    List<TravelCourseEntity> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    List<TravelCourseEntity> findByRegionCode(String regionCode);
}