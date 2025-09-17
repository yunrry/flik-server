package yunrry.flik.core.domain.model.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.core.domain.model.plan.SlotType;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSlot {

    private int day;
    private int slot;
    private SlotType slotType;
    private MainCategory mainCategory;
    private List<Long> recommendedSpotIds;
    private Long selectedSpotId;
    private Boolean isContinue;

    public static CourseSlot empty(int day, int slot) {
        return CourseSlot.builder()
                .day(day)
                .slot(slot)
                .slotType(SlotType.FREE_TIME)
                .recommendedSpotIds(List.of())
                .build();
    }

    public static CourseSlot of(int day, int slot, SlotType slotType, MainCategory mainCategory, List<Long> spotIds) {
        return CourseSlot.builder()
                .day(day)
                .slot(slot)
                .slotType(slotType)
                .mainCategory(mainCategory)
                .recommendedSpotIds(spotIds != null ? spotIds : List.of())
                .build();
    }

    public boolean isEmpty() {
        return slotType == SlotType.FREE_TIME ||
                (recommendedSpotIds == null || recommendedSpotIds.isEmpty());
    }

    public boolean hasRecommendations() {
        return recommendedSpotIds != null && !recommendedSpotIds.isEmpty();
    }

    public boolean hasSelectedSpot() {
        return selectedSpotId != null;
    }

    public void selectSpot(Long spotId) {
        if (recommendedSpotIds != null && recommendedSpotIds.contains(spotId)) {
            this.selectedSpotId = spotId;
        }
    }

    public String getSlotName() {
        return switch (slot) {
            case 0 -> "카페";
            case 1 -> "관광1";
            case 2 -> "점심";
            case 3 -> "관광2";
            case 4 -> "저녁";
            case 5 -> "관광3/숙박";
            default -> "알 수 없음";
        };
    }
}


