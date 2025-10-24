package yunrry.flik.core.service.travelPlanner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import yunrry.flik.core.service.plan.TravelPlannerService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TravelPlannerServiceTest {

    @InjectMocks
    private TravelPlannerService travelPlannerService;

    @Test
    @DisplayName("TC-801: 표준 2박3일 여행")
    void testStandardThreeDaysTrip() {
        // Given
        String[] userSelect = {"cafe", "nature", "history"};
        int day = 3;
        Map<String, Integer> saveCount = Map.of(
                "cafe", 15,
                "nature", 20,
                "history", 8
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        // 1. 모든 날 첫 슬롯 카페
        for (int i = 0; i < 3; i++) {
            assertEquals("cafe", result[i][0],
                    String.format("DAY%d 첫 슬롯 카페", i + 1));
        }

        // 2. 자연이 문화보다 먼저 등장
        int natureIdx = findFirstIndex(result, "nature");
        int historyIdx = findFirstIndex(result, "history");
        assertTrue(natureIdx < historyIdx, "자연 우선 배치");

        // 3. 숙박 2개 (DAY1, DAY2 마지막)
        assertEquals("accommodation", result[0][5], "DAY1 숙박");
        assertEquals("accommodation", result[1][5], "DAY2 숙박");

        // 4. DAY3 마지막 빈칸
        assertEquals("", result[2][5], "DAY3 마지막 빈칸");
    }

    @Test
    @DisplayName("TC-802: 액티비티 중심 1박2일")
    void testActivityCentricTwoDays() {
        // Given
        String[] userSelect = {"activity", "cafe", "nature"};
        int day = 2;
        Map<String, Integer> saveCount = Map.of(
                "activity", 10,
                "cafe", 5,
                "nature", 3
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        // 1. DAY1 첫 슬롯 카페
        assertEquals("cafe", result[0][0], "카페 고정");

        // 2. 액티비티 배치
        assertTrue(hasCategory(result, "activity"), "액티비티 배치");

        // 3. 액티비티가 자연보다 먼저
        int activityIdx = findFirstIndex(result, "activity");
        int natureIdx = findFirstIndex(result, "nature");
        assertTrue(activityIdx < natureIdx, "액티비티 우선");

        // 4. 숙박 배치
        assertEquals("accommodation", result[0][5], "숙박 배치");
    }

    @Test
    @DisplayName("TC-803: 축제+테마파크 2박3일")
    void testMultipleSpecialCategories() {
        // Given
        String[] userSelect = {"festival", "themepark", "cafe"};
        int day = 3;
        Map<String, Integer> saveCount = Map.of(
                "festival", 10,
                "themepark", 8,
                "cafe", 5
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        // 1. 카페 우선 배치
        for (int i = 0; i < 3; i++) {
            assertEquals("cafe", result[i][0], "매일 카페");
        }

        // 2. 축제, 테마파크 모두 배치
        assertTrue(hasCategory(result, "festival"), "축제 포함");
        assertTrue(hasCategory(result, "themepark"), "테마파크 포함");

        // 3. 축제가 테마파크보다 먼저
        int festivalIdx = findFirstIndex(result, "festival");
        int themeparkIdx = findFirstIndex(result, "themepark");
        assertTrue(festivalIdx < themeparkIdx, "빈도 반영");
    }

    // 헬퍼 메서드
    private int findFirstIndex(String[][] course, String category) {
        for (int i = 0; i < course.length; i++) {
            for (int j = 0; j < course[i].length; j++) {
                if (category.equals(course[i][j])) {
                    return i * 10 + j;
                }
            }
        }
        return -1;
    }

    private boolean hasCategory(String[][] course, String category) {
        return Arrays.stream(course)
                .flatMap(Arrays::stream)
                .anyMatch(slot -> category.equals(slot));
    }

    @Test
    @DisplayName("TC-701: 최대 일정")
    void testMaximumDays() {
        // Given
        String[] userSelect = {"nature", "history", "cafe", "market"};
        int day = 3;
        Map<String, Integer> saveCount = Map.of(
                "nature", 10,
                "history", 8,
                "cafe", 6,
                "market", 5
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        assertEquals(3, result.length, "3일");
        int totalSlots = Arrays.stream(result).mapToInt(arr -> arr.length).sum();
        assertEquals(18, totalSlots, "총 18개 슬롯");
    }

    @Test
    @DisplayName("TC-703: 당일치기제외 마지막 날 마지막 슬롯")
    void testLastDayLastSlot() {
        // Given
        String[] userSelect = {"nature", "cafe"};
        int[] days = {2, 3};
        Map<String, Integer> saveCount = Map.of(
                "nature", 10,
                "cafe", 5
        );

        for (int day : days) {
            // When
            String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

            // Then
            String lastSlot = result[result.length - 1][5];
            assertEquals("", lastSlot,
                    String.format("%d일 여행 마지막 슬롯 빈칸", day));
        }
    }

    @Test
    @DisplayName("TC-704: 카페 + 특별 카테고리 동시")
    void testCafeWithSpecialCategory() {
        // Given
        String[] userSelect = {"cafe", "festival"};
        int day = 1;
        Map<String, Integer> saveCount = Map.of(
                "cafe", 10,
                "festival", 8
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        assertEquals("cafe", result[0][0], "첫 슬롯 카페");
        assertTrue(Arrays.asList(result[0]).contains("festival"), "축제 배치");
    }
}