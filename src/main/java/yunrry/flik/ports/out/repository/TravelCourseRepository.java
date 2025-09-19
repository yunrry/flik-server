package yunrry.flik.ports.out.repository;

import yunrry.flik.core.domain.model.plan.TravelCourse;
import yunrry.flik.core.domain.model.plan.CourseType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TravelCourseRepository {

    TravelCourse save(TravelCourse travelCourse);

    Optional<TravelCourse> findById(Long id);

    List<TravelCourse> findByUserId(Long userId);

    List<TravelCourse> findByUserIdAndCourseType(Long userId, CourseType courseType);

    List<TravelCourse> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime createdAt);

    void deleteById(Long id);

    void deleteByUserId(Long userId);

    boolean existsByUserIdAndCourseType(Long userId, CourseType courseType);

    List<TravelCourse> findByRegionCode(String regionCode);
}