package yunrry.flik.core.domain.model;

public enum MainCategory {

    NATURE("nature", "자연"),
    INDOOR("indoor", "실내"),
    HISTORY_CULTURE("history_culture", "역사문화"),
    CAFE("cafe", "카페"),
    ACTIVITY("activity", "액티비티"),
    FESTIVAL("festival", "축제"),
    MARKET("market", "시장"),
    THEMEPARK("themepark", "테마파크"),
    RESTAURANT("restaurant", "맛집"),
    ACCOMMODATION("accommodation", "숙박");

    private final String code;
    private final String displayName;

    MainCategory(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 코드로 카테고리 찾기
     */
    public static MainCategory findByCode(String code) {
        for (MainCategory category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        return null;
    }

    public static MainCategory findByDisplayName(String displayName) {
        for (MainCategory category : values()) {
            if (category.getDisplayName().equals(displayName)) {
                return category;
            }
        }
        return null;
    }

    public static MainCategory of(String displayName) {
        return findByDisplayName(displayName);
    }



}