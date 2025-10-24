package yunrry.flik.core.service.travelPlanner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import yunrry.flik.core.service.plan.TravelPlannerService;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TravelPlannerServiceTestAccommodation {

    @InjectMocks
    private TravelPlannerService travelPlannerService;

    @Test
    @DisplayName("TC-301: 당일치기 숙박 없음")
    void testNoAccommodationInOneDay() {
        // Given
        String[] userSelect = {"nature", "cafe"};
        int day = 1;
        Map<String, Integer> saveCount = Map.of(
                "nature", 5,
                "cafe", 3
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        boolean hasAccommodation = Arrays.stream(result[0])
                .anyMatch(slot -> "accommodation".equals(slot));
        assertFalse(hasAccommodation, "숙박 없음");
    }

    @Test
    @DisplayName("TC-302: 1박2일 숙박 배치")
    void testAccommodationInTwoDays() {
        // Given
        String[] userSelect = {"nature", "history"};
        int day = 2;
        Map<String, Integer> saveCount = Map.of(
                "nature", 8,
                "history", 6
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        assertEquals("accommodation", result[0][5], "DAY1 마지막 슬롯 숙박");

        long accommodationCount = Arrays.stream(result)
                .flatMap(Arrays::stream)
                .filter(slot -> "accommodation".equals(slot))
                .count();
        assertEquals(1, accommodationCount, "숙박 1개");
    }

    @Test
    @DisplayName("TC-303: 2박3일 숙박 배치")
    void testAccommodationInThreeDays() {
        // Given
        String[] userSelect = {"nature", "history", "cafe"};
        int day = 3;
        Map<String, Integer> saveCount = Map.of(
                "nature", 10,
                "history", 8,
                "cafe", 6
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        assertEquals("accommodation", result[0][5], "DAY1 숙박");
        assertEquals("accommodation", result[1][5], "DAY2 숙박");
        assertNotEquals("accommodation", result[2][5], "DAY3 숙박 없음");

        long accommodationCount = Arrays.stream(result)
                .flatMap(Arrays::stream)
                .filter(slot -> "accommodation".equals(slot))
                .count();
        assertEquals(2, accommodationCount, "숙박 2개");
    }
}