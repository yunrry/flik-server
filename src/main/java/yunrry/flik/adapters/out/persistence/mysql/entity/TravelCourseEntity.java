package yunrry.flik.adapters.out.persistence.mysql.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yunrry.flik.core.domain.model.plan.CourseSlot;
import yunrry.flik.core.domain.model.plan.CourseType;
import yunrry.flik.core.domain.model.plan.TravelCourse;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


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

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "days", nullable = false)
    private int days;

    @Column(name = "total_distance")
    private Double totalDistance;


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

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @Transient
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static TravelCourseEntity from(TravelCourse travelCourse, String courseSlotsJson, String selectedCategoriesJson) {
        return TravelCourseEntity.builder()
                .id(travelCourse.getId())
                .userId(travelCourse.getUserId())
                .name(travelCourse.getName())
                .days(travelCourse.getDays())
                .totalDistance(travelCourse.getTotalDistance())
                .courseSlotsJson(courseSlotsJson)
                .createdAt(travelCourse.getCreatedAt())
                .courseType(travelCourse.getCourseType())
                .regionCode(travelCourse.getRegionCode())
                .selectedCategories(selectedCategoriesJson)
                .isPublic(travelCourse.getIsPublic())
                .build();
    }

    public TravelCourse toDomain(CourseSlot[][] courseSlots) {
        List<String> parsedCategories = parseSelectedCategories(this.selectedCategories);

        return TravelCourse.builder()
                .id(this.id)
                .userId(this.userId)
                .name(this.name)
                .days(this.days)
                .totalDistance(this.totalDistance)
                .courseSlots(courseSlots)
                .createdAt(this.createdAt)
                .courseType(this.courseType)
                .regionCode(this.regionCode)
                .selectedCategories(parsedCategories)
                .isPublic(this.isPublic)
                .build();
    }

    private List<String> parseSelectedCategories(String rawCategories) {
        if (rawCategories == null || rawCategories.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            String trimmed = rawCategories.trim();

            // JSON 배열 형태 (올바른 형태)
            if (trimmed.startsWith("[\"") && trimmed.endsWith("\"]")) {
                return objectMapper.readValue(trimmed, new TypeReference<List<String>>() {});
            }

            // toString() 결과 형태 [item1, item2, item3] (잘못된 형태)
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                String content = trimmed.substring(1, trimmed.length() - 1);
                return Arrays.stream(content.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());
            }

            return Collections.singletonList(trimmed);

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse selectedCategories: " + rawCategories, e);
        }
    }
}