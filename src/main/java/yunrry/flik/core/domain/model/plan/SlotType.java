package yunrry.flik.core.domain.model.plan;

import yunrry.flik.core.domain.model.MainCategory;

public enum SlotType {
    TOURISM("관광"),
    RESTAURANT("식사"),
    CAFE("카페"),
    ACCOMMODATION("숙박"),
    FREE_TIME("여유시간");

    private final String displayName;

    SlotType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SlotType fromMainCategory(MainCategory mainCategory) {
        return switch (mainCategory) {
            case RESTAURANT -> RESTAURANT;
            case CAFE -> CAFE;
            case ACCOMMODATION -> ACCOMMODATION;
            default -> TOURISM;
        };
    }
}