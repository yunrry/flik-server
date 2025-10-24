package yunrry.flik.core.service.travelPlanner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import yunrry.flik.core.service.plan.TravelPlannerService;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TravelPlannerServiceTestWhenPriority {

    @InjectMocks
    private TravelPlannerService travelPlannerService;

    @Test
    @DisplayName("TC-501: 빈도순 배치 - 첫 등장 순서")
    void testFirstAppearanceByFrequency() {
        // Given
        String[] userSelect = {"nature", "history", "market"};
        int day = 1;
        Map<String, Integer> saveCount = Map.of(
                "nature", 20,
                "market", 10,
                "history", 5
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        int natureIdx = findFirstIndex(result, "nature");
        int marketIdx = findFirstIndex(result, "market");
        int historyIdx = findFirstIndex(result, "history");

        assertTrue(natureIdx < marketIdx, "자연이 전통시장보다 먼저");
        assertTrue(marketIdx < historyIdx, "전통시장이 문화보다 먼저");
    }

    @Test
    @DisplayName("TC-502: 동일 빈도 - 일관성")
    void testSameFrequencyConsistency() {
        // Given
        String[] userSelect = {"nature", "history"};
        int day = 1;
        Map<String, Integer> saveCount = Map.of(
                "nature", 10,
                "history", 10
        );

        // When
        String[][] result1 = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);
        String[][] result2 = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        assertArrayEquals(result1[0], result2[0], "일관된 순서");
    }

    @Test
    @DisplayName("TC-503: 라운드로빈 검증")
    void testRoundRobinPattern() {
        // Given
        String[] userSelect = {"nature", "history"};
        int day = 3;
        Map<String, Integer> saveCount = Map.of(
                "nature", 3,
                "history", 3
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        List<String> touristSlots = extractTouristSlots(result);

        // 교대 패턴 검증
        for (int i = 0; i < touristSlots.size() - 1; i += 2) {
            assertNotEquals(touristSlots.get(i), touristSlots.get(i + 1),
                    "라운드로빈 교대");
        }
    }

    @Test
    @DisplayName("TC-504: 특별+일반 통합 순서")
    void testSpecialNormalOrder() {
        // Given
        String[] userSelect = {"nature", "activity"};
        int day = 3;
        Map<String, Integer> saveCount = Map.of(
                "nature", 20,
                "activity", 15
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        int natureIdx = findFirstIndex(result, "nature");
        int activityIdx = findFirstIndex(result, "activity");

        assertTrue(natureIdx < activityIdx, "자연이 액티비티보다 먼저");
    }

    @Test
    @DisplayName("TC-505: 카페+빈도순 슬롯 배치")
    void testCafeWithFrequencySlots() {
        // Given
        String[] userSelect = {"cafe", "nature", "history"};
        int day = 1;
        Map<String, Integer> saveCount = Map.of(
                "cafe", 10,
                "nature", 15,
                "history", 8
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        assertEquals("cafe", result[0][0], "슬롯[0] 카페");
        assertEquals("nature", result[0][1], "슬롯[1] 자연");
        assertEquals("history", result[0][3], "슬롯[3] 문화");
    }

    @Test
    @DisplayName("TC-506: 특별 카테고리 빈도 반영")
    void testSpecialCategoryByFrequency() {
        // Given
        String[] userSelect = {"festival", "nature", "history"};
        int day = 1;
        Map<String, Integer> saveCount = Map.of(
                "festival", 5,
                "nature", 20,
                "history", 15
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        int natureIdx = findFirstIndex(result, "nature");
        int historyIdx = findFirstIndex(result, "history");
        int festivalIdx = findFirstIndex(result, "festival");

        assertTrue(natureIdx < historyIdx, "자연이 문화보다 먼저");
        assertTrue(historyIdx < festivalIdx, "문화가 축제보다 먼저");
    }

    @Test
    @DisplayName("TC-507: 극단적 차이 순서")
    void testExtremeDifferenceOrder() {
        // Given
        String[] userSelect = {"nature", "history"};
        int day = 1;
        Map<String, Integer> saveCount = Map.of(
                "nature", 50,
                "history", 1
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        int natureIdx = findFirstIndex(result, "nature");
        int historyIdx = findFirstIndex(result, "history");

        assertTrue(natureIdx < historyIdx, "자연이 문화보다 먼저");
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

    private List<String> extractTouristSlots(String[][] course) {
        List<String> slots = new ArrayList<>();
        for (String[] day : course) {
            if (!day[1].isEmpty() && !day[1].equals("restaurant") && !day[1].equals("accommodation")) {
                slots.add(day[1]);
            }
            if (!day[3].isEmpty() && !day[3].equals("restaurant") && !day[3].equals("accommodation")) {
                slots.add(day[3]);
            }
            if (!day[5].isEmpty() && !day[5].equals("restaurant") && !day[5].equals("accommodation")) {
                slots.add(day[5]);
            }
        }
        return slots;
    }
}