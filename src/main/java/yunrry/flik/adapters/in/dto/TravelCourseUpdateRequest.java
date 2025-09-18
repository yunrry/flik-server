package yunrry.flik.adapters.in.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import yunrry.flik.core.domain.model.plan.CourseSlot;

import java.util.List;

@Getter
@NoArgsConstructor
public class TravelCourseUpdateRequest {
    private Double totalDistance;
    private CourseSlot[][] courseSlots;
    private String regionCode;
    private List<String> selectedCategories;
}
