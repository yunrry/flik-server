package yunrry.flik.core.domain.model.plan;

public enum CourseType {
    PERSONALIZED("개인화"),
    REGIONAL("지역 기반"),
    CUSTOM("사용자 정의");

    private final String displayName;

    CourseType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}