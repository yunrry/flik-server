package yunrry.flik.ports.in.usecase;

import yunrry.flik.adapters.in.dto.TravelCourseUpdateRequest;
import yunrry.flik.core.domain.model.plan.TravelCourse;
import yunrry.flik.ports.in.query.TravelCourseQuery;

import java.util.List;

public interface TravelCourseUseCase {
    TravelCourse getTravelCourse(Long courseId);

    List<TravelCourse>getTravelCoursesByUserId(Long userId);

    TravelCourse updateTravelCourse(Long id, TravelCourseUpdateRequest request);

    void deleteTravelCourse(Long courseId);

    List<TravelCourse> getTravelCoursesByRegionCode(String regionCode);
}
