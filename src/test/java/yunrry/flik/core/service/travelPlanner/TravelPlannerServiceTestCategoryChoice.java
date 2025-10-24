package yunrry.flik.core.service.travelPlanner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import yunrry.flik.core.service.plan.TravelPlannerService;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TravelPlannerServiceTestCategoryChoice {

    @InjectMocks
    private TravelPlannerService travelPlannerService;

    private Map<String, Integer> testSaveCount;

    @Test
    @DisplayName("TC-101: 최소 카테고리 선택 (2개)")
    void testMinimumCategorySelection() {
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
        assertNotNull(result, "코스 생성 성공");
        assertTrue(result.length > 0, "코스 존재");
    }

    @Test
    @DisplayName("TC-102: 최대 카테고리 선택 (4개)")
    void testMaximumCategorySelection() {
        // Given
        String[] userSelect = {"nature", "cafe", "history", "market"};
        int day = 3;
        Map<String, Integer> saveCount = Map.of(
                "nature", 10,
                "cafe", 8,
                "history", 6,
                "market", 5
        );

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, saveCount);

        // Then
        assertNotNull(result, "코스 생성 성공");

        // 모든 카테고리 포함 여부
        Set<String> allSlots = Arrays.stream(result)
                .flatMap(Arrays::stream)
                .collect(Collectors.toSet());

        assertTrue(allSlots.contains("nature"), "자연 포함");
        assertTrue(allSlots.contains("cafe"), "카페 포함");
        assertTrue(allSlots.contains("history"), "문화 포함");
        assertTrue(allSlots.contains("market"), "전통시장 포함");
    }

    @Test
    @DisplayName("TC-103: 카테고리 부족 (1개)")
    void testInsufficientCategories() {
        // Given
        String[] userSelect = {"nature"};
        int day = 1;
        Map<String, Integer> saveCount = Map.of("nature", 5);

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> travelPlannerService.generateTravelCourse(userSelect, day, saveCount),
                "최소 2개 카테고리 필요");
    }

    @Test
    @DisplayName("TC-104: 카테고리 초과 (5개)")
    void testExcessiveCategories() {
        // Given
        String[] userSelect = {"nature", "cafe", "history", "market", "indoor"};
        int day = 3;
        Map<String, Integer> saveCount = Map.of(
                "nature", 10,
                "cafe", 8,
                "history", 6,
                "market", 5,
                "indoor", 4
        );

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> travelPlannerService.generateTravelCourse(userSelect, day, saveCount),
                "최대 4개 카테고리 제한");
    }
}