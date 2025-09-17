package yunrry.flik.core.domain.model.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yunrry.flik.core.domain.model.card.Spot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelCourse {

    private Long userId;
    private int days;
    private CourseSlot[][] courseSlots;
    private LocalDateTime createdAt;
    private CourseType courseType;

    public static TravelCourse of(Long userId, int days, CourseSlot[][] courseSlots) {
        return TravelCourse.builder()
                .userId(userId)
                .days(days)
                .courseSlots(courseSlots)
                .courseType(CourseType.PERSONALIZED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static TravelCourse ofRegion(Long userId, int days, CourseSlot[][] courseSlots) {
        return TravelCourse.builder()
                .userId(userId)
                .days(days)
                .courseSlots(courseSlots)
                .courseType(CourseType.REGIONAL)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public int getTotalSlots() {
        return days * 6;
    }

    public int getFilledSlots() {
        int count = 0;
        for (CourseSlot[] day : courseSlots) {
            for (CourseSlot slot : day) {
                if (!slot.isEmpty()) {
                    count++;
                }
            }
        }
        return count;
    }

    public List<Long> getAllRecommendedSpotIds() {
        List<Long> allSpotIds = new ArrayList<>();

        for (CourseSlot[] day : courseSlots) {
            for (CourseSlot slot : day) {
                if (slot.hasRecommendations()) {
                    allSpotIds.addAll(slot.getRecommendedSpotIds());
                }
            }
        }
        return allSpotIds;
    }

    public CourseSlot getSlot(int day, int slotIndex) {
        if (day >= 0 && day < days && slotIndex >= 0 && slotIndex < 6) {
            return courseSlots[day][slotIndex];
        }
        return null;
    }

    public void selectSpotForSlot(int day, int slotIndex, Long spotId) {
        CourseSlot slot = getSlot(day, slotIndex);
        if (slot != null) {
            slot.selectSpot(spotId);
        }
    }
}