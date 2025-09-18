package yunrry.flik.adapters.out.persistence.mysql.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yunrry.flik.core.domain.model.plan.CourseSlot;
import yunrry.flik.core.domain.model.plan.CourseType;
import yunrry.flik.core.domain.model.plan.TravelCourse;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "travel_courses")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelCourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "days", nullable = false)
    private int days;

    @Column(name = "total_distance")
    private Double totalDistance;

    @Lob
    @Column(name = "course_slots_json", columnDefinition = "TEXT")
    private String courseSlotsJson; // JSON string representation of CourseSlot[][]

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", nullable = false)
    private CourseType courseType;

    @Column(name = "region_code")
    private String regionCode;

    @Column(name = "selected_categories")
    private String selectedCategories; // JSON array of categories

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static TravelCourseEntity from(TravelCourse travelCourse, String courseSlotsJson) {
        return TravelCourseEntity.builder()
                .userId(travelCourse.getUserId())
                .days(travelCourse.getDays())
                .totalDistance(travelCourse.getTotalDistance())
                .courseSlotsJson(courseSlotsJson)
                .createdAt(travelCourse.getCreatedAt())
                .courseType(travelCourse.getCourseType())
                .regionCode(travelCourse.getRegionCode())
                .selectedCategories(travelCourse.getSelectedCategories().toString())
                .build();
    }

    public TravelCourse toDomain(CourseSlot[][] courseSlots) {
        return TravelCourse.builder()
                .id(this.id)
                .userId(this.userId)
                .days(this.days)
                .totalDistance(this.totalDistance)
                .courseSlots(courseSlots)
                .createdAt(this.createdAt)
                .courseType(this.courseType)
                .regionCode(this.regionCode)
                .selectedCategories(List.of(this.selectedCategories))
                .build();
    }
}