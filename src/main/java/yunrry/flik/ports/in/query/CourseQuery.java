package yunrry.flik.ports.in.query;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Builder
@RequiredArgsConstructor
public class CourseQuery {
    private final Long userId;
    private final List<String> selectedCategories;
    private final String selectedRegion;
    private final int days;


    public static CourseQuery of(Long userId, List<String> selectedCategories, String selectedRegion, int days) {
        return CourseQuery.builder()
                .userId(userId)
                .selectedCategories(selectedCategories)
                .selectedRegion(selectedRegion)
                .days(days)
                .build();
    }

    public Long getUserId() {
        return userId;
    }

    public List<String> getSelectedCategories() {
        return selectedCategories;
    }

    public String getSelectedRegion() {
        return selectedRegion;
    }

    public int getDays() {
        return days;
    }
}
