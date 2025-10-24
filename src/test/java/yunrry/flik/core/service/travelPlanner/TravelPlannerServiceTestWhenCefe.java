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
class TravelPlannerServiceTestWhenCefe {

    @InjectMocks
    private TravelPlannerService travelPlannerService;

    @Test
    @DisplayName("TC-201: 카페 포함 당일치기")
    void testCafeInOneDayTrip() {
        // Given
        String[] userSelect = {"cafe", "nature"};
        int day = 1;
        Map<String, Integer> saveCount = Map.of(
                "cafe", 5,
                "nature", 8
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        assertEquals("cafe", result[0][0], "첫 슬롯 카페");
    }

    @Test
    @DisplayName("TC-202: 카페 포함 2박3일")
    void testCafeInThreeDaysTrip() {
        // Given
        String[] userSelect = {"cafe", "nature", "history"};
        int day = 3;
        Map<String, Integer> saveCount = Map.of(
                "cafe", 10,
                "nature", 8,
                "history", 6
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        for (int i = 0; i < result.length; i++) {
            assertEquals("cafe", result[i][0],
                    String.format("DAY%d 첫 슬롯 카페", i + 1));
        }

        // 카페 총 개수
        long cafeCount = Arrays.stream(result)
                .flatMap(Arrays::stream)
                .filter(slot -> "cafe".equals(slot))
                .count();
        assertEquals(3, cafeCount, "카페 3개 (각 날 첫 슬롯)");
    }

    @Test
    @DisplayName("TC-203: 카페 미포함")
    void testWithoutCafe() {
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
        assertNotEquals("cafe", result[0][0], "첫 슬롯 카페 아님");
        assertNotEquals("cafe", result[1][0], "첫 슬롯 카페 아님");
    }
}