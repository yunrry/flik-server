package yunrry.flik.adapters.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yunrry.flik.core.domain.model.plan.CourseSlot;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSlotResponse {

    private int day;
    private int slot;
    private String slotType;
    private String mainCategory;
    private String slotName;
    private List<Long> recommendedSpotIds;
    private Long selectedSpotId;
    private boolean isEmpty;
    private boolean hasRecommendations;
    private boolean hasSelectedSpot;

    public static CourseSlotResponse from(CourseSlot courseSlot) {
        return CourseSlotResponse.builder()
                .day(courseSlot.getDay()) // 0-base to 1-base
                .slot(courseSlot.getSlot())
                .slotType(courseSlot.getSlotType() != null ? courseSlot.getSlotType().name() : null)
                .mainCategory(courseSlot.getMainCategory() != null ? courseSlot.getMainCategory().name() : null)
                .slotName(courseSlot.getSlotName())
                .recommendedSpotIds(courseSlot.getRecommendedSpotIds())
                .selectedSpotId(courseSlot.getSelectedSpotId())
                .isEmpty(courseSlot.isEmpty())
                .hasRecommendations(courseSlot.hasRecommendations())
                .hasSelectedSpot(courseSlot.hasSelectedSpot())
                .build();
    }

    public static List<CourseSlotResponse> from(List<CourseSlot> courseSlots) {
        return courseSlots.stream()
                .map(CourseSlotResponse::from)
                .toList();
    }
}