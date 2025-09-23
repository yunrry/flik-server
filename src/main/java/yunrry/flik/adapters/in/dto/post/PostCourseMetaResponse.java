package yunrry.flik.adapters.in.dto.post;

import yunrry.flik.core.domain.model.PostMetadata;
import yunrry.flik.core.domain.model.plan.CourseSlot;
import yunrry.flik.core.domain.model.plan.CourseType;
import yunrry.flik.core.domain.model.plan.TravelCourse;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public record PostCourseMetaResponse(
        Long courseId,
        List<Long> spotIds,
        String name,
        int days,
        String regionCode,
        List<String> categories
) {
    public static PostCourseMetaResponse from(TravelCourse travelCourse) {
        if (travelCourse == null) return null;

        return new PostCourseMetaResponse(
                travelCourse.getId(),
                Arrays.stream(travelCourse.getCourseSlots())
                        .flatMap(Arrays::stream)
                        .filter(slot -> slot.getSelectedSpotId() != null)
                        .map(CourseSlot::getSelectedSpotId)
                        .toList(),
                travelCourse.getName(),
                travelCourse.getDays(),
                travelCourse.getRegionCode(),
                travelCourse.getSelectedCategories()
        );
    }


}