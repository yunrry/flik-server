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
class TravelPlannerServiceTestBasic {

    @InjectMocks
    private TravelPlannerService travelPlannerService;

    private Map<String, Integer> testSaveCount;

    @Test
    @DisplayName("TC-001: 당일치기 기본 슬롯 구조")
    void testOneDayTripBasicStructure() {
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
        assertEquals(1, result.length, "1일 코스");
        assertEquals(6, result[0].length, "6개 슬롯");

        long restaurantCount = Arrays.stream(result[0])
                .filter(slot -> "restaurant".equals(slot))
                .count();
        assertEquals(2, restaurantCount, "식당 2개");

        assertFalse(Arrays.stream(result[0]).anyMatch(s -> "accommodation".equals(s)),
                "숙박 없음");
    }

    @Test
    @DisplayName("TC-002: 1박2일 기본 슬롯 구조")
    void testTwoDaysTripBasicStructure() {
        // Given
        String[] userSelect = {"history", "cafe"};
        int day = 2;
        Map<String, Integer> saveCount = Map.of(
                "history", 8,
                "cafe", 5
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        assertEquals(2, result.length, "2일 코스");

        // DAY1 검증
        assertEquals("accommodation", result[0][5], "DAY1 마지막 슬롯 숙박");

        // DAY2 검증
        assertEquals("", result[1][5], "DAY2 마지막 슬롯 빈칸");
        assertNotEquals("accommodation", result[1][5], "DAY2 숙박 없음");
    }

    @Test
    @DisplayName("TC-003: 2박3일 기본 슬롯 구조")
    void testThreeDaysTripBasicStructure() {
        // Given
        String[] userSelect = {"nature", "history"};
        int day = 3;
        Map<String, Integer> saveCount = Map.of(
                "nature", 10,
                "history", 8
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        assertEquals(3, result.length, "3일 코스");
        assertEquals(18, Arrays.stream(result).mapToInt(arr -> arr.length).sum(),
                "총 18개 슬롯");

        // 숙박 검증
        assertEquals("accommodation", result[0][5], "DAY1 숙박");
        assertEquals("accommodation", result[1][5], "DAY2 숙박");
        assertEquals("", result[2][5], "DAY3 마지막 빈칸");

        // 숙박 개수
        long accommodationCount = Arrays.stream(result)
                .flatMap(Arrays::stream)
                .filter(slot -> "accommodation".equals(slot))
                .count();
        assertEquals(2, accommodationCount, "숙박 2개");
    }

    @Test
    @DisplayName("TC-004: 하루 식당 개수 검증")
    void testRestaurantCountPerDay() {
        // Given
        String[] userSelect = {"nature", "history"};
        int day = 3;
        Map<String, Integer> saveCount = Map.of(
                "nature", 10,
                "history", 8
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        for (int i = 0; i < result.length; i++) {
            long restaurantCount = Arrays.stream(result[i])
                    .filter(slot -> "restaurant".equals(slot))
                    .count();
            assertEquals(2, restaurantCount,
                    String.format("DAY%d 식당 2개", i + 1));
        }
    }

}