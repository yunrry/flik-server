package yunrry.flik.adapters.in.dto;
import lombok.Getter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import yunrry.flik.core.domain.model.plan.CourseType;
import yunrry.flik.core.domain.model.plan.TravelCourse;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelCourseResponse {
    private Long id;
    private Long userId;
    private int days;
    private String regionCode;
    private Double totalDistance;
    private CourseSlotResponse[][] courseSlots;
    private LocalDateTime createdAt;
    private String courseType;
    private int totalSlots;
    private int filledSlots;
    private List<String> selectedCategories;
    private Boolean isPublic;

    public static TravelCourseResponse from(TravelCourse travelCourse) {
        CourseSlotResponse[][] courseSlotResponses = new CourseSlotResponse[travelCourse.getDays()][];

        for (int day = 0; day < travelCourse.getDays(); day++) {
            // 실제 도메인의 슬롯 개수만큼 생성
            int slotsForThisDay = travelCourse.getCourseSlots()[day].length;
            courseSlotResponses[day] = new CourseSlotResponse[slotsForThisDay];

            for (int slot = 0; slot < slotsForThisDay; slot++) {
                courseSlotResponses[day][slot] = CourseSlotResponse.from(
                        travelCourse.getCourseSlots()[day][slot]
                );
            }
        }

        return TravelCourseResponse.builder()
                .id(travelCourse.getId())
                .userId(travelCourse.getUserId())
                .days(travelCourse.getDays())
                .regionCode(travelCourse.getRegionCode())
                .totalDistance(travelCourse.getTotalDistance())
                .courseSlots(courseSlotResponses)
                .createdAt(travelCourse.getCreatedAt())
                .courseType(travelCourse.getCourseType() != null ? travelCourse.getCourseType().name() : null)
                .totalSlots(travelCourse.getTotalSlots())
                .filledSlots(travelCourse.getFilledSlots())
                .selectedCategories(travelCourse.getSelectedCategories())
                .isPublic(travelCourse.getIsPublic())
                .build();
    }

    public static List<TravelCourseResponse> from(List<TravelCourse> travelCourses) {
        return travelCourses.stream()
                .map(TravelCourseResponse::from)
                .toList();
    }
}