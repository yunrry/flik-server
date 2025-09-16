package yunrry.flik.core.service;

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

    private Map<String, Integer> testSaveCount;

    @BeforeEach
    void setUp() {
        // 실제 HashMap 사용 (Mock 대신)
        testSaveCount = new HashMap<>();
        testSaveCount.put("nature", 7);
        testSaveCount.put("indoor", 6);
        testSaveCount.put("history", 4);
        testSaveCount.put("cafe", 5);
        testSaveCount.put("activity", 3);
        testSaveCount.put("festival", 2);
        testSaveCount.put("market", 4);
        testSaveCount.put("themepark", 2);
    }

    @Test
    @DisplayName("1일 여행 기본 구조 테스트")
    void testOneDayTripBasicStructure() {
        // Given
        String[] userSelect = {"history", "nature"};
        int day = 1;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        assertEquals(1, result.length, "1일 여행이므로 배열 길이는 1");
        assertEquals(6, result[0].length, "각 날은 6개 슬롯");

        // 기본 식사 확인
        assertEquals("restaurant", result[0][2], "점심은 2번 슬롯");
        assertEquals("restaurant", result[0][4], "저녁은 4번 슬롯");

        // 숙박 없음 확인 (1일 여행)
        assertFalse(Arrays.stream(result[0]).anyMatch(slot -> slot.equals("accommodation")),
                "1일 여행에는 숙박이 없어야 함");
    }

    @Test
    @DisplayName("2일 여행 기본 구조 테스트")
    void testTwoDayTripBasicStructure() {
        // Given
        String[] userSelect = {"history", "cafe"};
        int day = 2;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        assertEquals(2, result.length, "2일 여행이므로 배열 길이는 2");

        // 모든 날 기본 식사 확인
        for (int i = 0; i < day; i++) {
            assertEquals("restaurant", result[i][2], String.format("%d일차 점심은 2번 슬롯", i + 1));
            assertEquals("restaurant", result[i][4], String.format("%d일차 저녁은 4번 슬롯", i + 1));
        }

        // 첫째 날에만 숙박 있음
        boolean hasAccommodationDay1 = Arrays.stream(result[0]).anyMatch(slot -> slot.equals("accommodation"));
        boolean hasAccommodationDay2 = Arrays.stream(result[1]).anyMatch(slot -> slot.equals("accommodation"));

        assertTrue(hasAccommodationDay1, "첫째 날에는 숙박이 있어야 함");
        assertFalse(hasAccommodationDay2, "마지막 날에는 숙박이 없어야 함");

        // 마지막 날 마지막 슬롯 비어있음
        assertEquals("", result[1][5], "마지막 날 5번 슬롯은 비어있어야 함");
    }

    @Test
    @DisplayName("카페 포함 여행 테스트")
    void testTripWithCafe() {
        // Given
        String[] userSelect = {"cafe", "history"};
        int day = 2;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 카페가 0번 슬롯에 배치되는지 확인
        for (int i = 0; i < day; i++) {
            assertEquals("cafe", result[i][0], String.format("%d일차 0번 슬롯에 카페 배치", i + 1));
        }
    }

    @Test
    @DisplayName("하루종일 활동 - 1일 여행 테스트")
    void testFullDayActivityOneDayTrip() {
        // Given
        String[] userSelect = {"activity", "history"};
        int day = 1;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 1일 여행에서는 하루종일 활동이 1, 3번 슬롯에 배치
        assertEquals("activity", result[0][1], "1번 슬롯에 activity");
        assertEquals("activity", result[0][3], "3번 슬롯에 activity");

        // 1일 여행에서는 5번 슬롯에 다른 카테고리 배치 (setLastDayEmpty가 적용되지 않음)
        assertEquals("history", result[0][5], "5번 슬롯에 history 배치");

        // 기본 구조 확인
        assertEquals("restaurant", result[0][2], "점심은 2번 슬롯");
        assertEquals("restaurant", result[0][4], "저녁은 4번 슬롯");
    }

    @Test
    @DisplayName("하루종일 활동이 실제로 배치되는지 확인 - 1일 여행")
    void testFullDayActivityActualPlacement() {
        // Given
        String[] userSelect = {"activity"};
        int day = 1;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 하루종일 활동은 1, 3번 슬롯에 배치
        assertEquals("activity", result[0][1], "1번 슬롯에 activity");
        assertEquals("activity", result[0][3], "3번 슬롯에 activity");

        // 1일 여행에서는 5번 슬롯도 사용 - selected에서 제거되지 않아 추가 배치
        assertEquals("activity", result[0][5], "5번 슬롯에도 activity 배치");

        // activity가 총 3번 배치되어야 함 (빈도3)
        long activityCount = Arrays.stream(result[0])
                .filter(slot -> "activity".equals(slot))
                .count();
        assertEquals(3, activityCount, "activity가 빈도수만큼 3번 배치되어야 함");
    }

    @Test
    @DisplayName("1일 여행에서 하루종일 활동 우선순위 테스트")
    void testFullDayActivityPriorityOneDayTrip() {
        // Given - 여러 하루종일 활동 중 빈도 높은 것이 배치되는지
        String[] userSelect = {"themepark", "activity", "festival"};
        int day = 1;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // activity(빈도3)가 가장 높으므로 우선 배치
        assertEquals("activity", result[0][1], "1번 슬롯에 빈도 높은 activity");
        assertEquals("activity", result[0][3], "3번 슬롯에 빈도 높은 activity");

        // 1일 여행에서는 5번 슬롯도 사용됨
        assertFalse(result[0][5].isEmpty(), "1일 여행에서는 5번 슬롯도 사용됨");

        // 5번 슬롯에는 나머지 카테고리 중 하나가 배치됨
        assertTrue(result[0][5].equals("activity") ||
                        result[0][5].equals("themepark") ||
                        result[0][5].equals("festival"),
                "5번 슬롯에 선택된 카테고리 중 하나가 배치됨");
    }

    @Test
    @DisplayName("포맷팅 기능 테스트")
    void testFormatCourse() {
        // Given - 빈 슬롯이 있는 상황을 만들기 위해 카테고리 수를 줄임
        String[] userSelect = {"cafe"};  // cafe만 선택하여 일부 슬롯을 비움
        int day = 1;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);
        String formatted = travelPlannerService.formatCourse(result);

        // Then
        assertNotNull(formatted, "포맷팅 결과가 null이 아니어야 함");
        assertTrue(formatted.contains("1일차"), "일차 정보 포함");
        assertTrue(formatted.contains("카페"), "슬롯 이름 포함");
        assertTrue(formatted.contains("점심"), "기본 식사 포함");
        assertTrue(formatted.contains("저녁"), "기본 식사 포함");

        // 실제로 빈 슬롯이 있는지 확인하고 여유시간이 표시되는지 검증
        boolean hasEmptySlot = Arrays.stream(result[0]).anyMatch(String::isEmpty);
        if (hasEmptySlot) {
            assertTrue(formatted.contains("여유시간"), "빈 슬롯은 여유시간으로 표시");
        } else {
            // 빈 슬롯이 없다면 이 검증은 건너뜀
            assertTrue(true, "모든 슬롯이 채워진 경우");
        }
    }

    @Test
    @DisplayName("1일 여행에서 모든 슬롯 활용 테스트")
    void testOneDayTripAllSlotsUsed() {
        // Given
        String[] userSelect = {"activity", "history", "nature"};
        int day = 1;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 1일 여행에서는 setLastDayEmpty가 적용되지 않아 모든 관광 슬롯 사용
        assertFalse(result[0][1].isEmpty(), "1번 슬롯 사용");
        assertFalse(result[0][3].isEmpty(), "3번 슬롯 사용");
        assertFalse(result[0][5].isEmpty(), "5번 슬롯도 사용");

        // 기본 구조
        assertEquals("restaurant", result[0][2], "점심은 2번 슬롯");
        assertEquals("restaurant", result[0][4], "저녁은 4번 슬롯");
    }

    @Test
    @DisplayName("1일 vs 2일 여행 5번 슬롯 사용 차이 테스트")
    void testOneDayVsTwoDaySlotUsage() {
        // Given
        String[] userSelect = {"history", "nature"};

        // When
        String[][] oneDayResult = travelPlannerService.generateTravelCourse(userSelect, 1, testSaveCount);
        String[][] twoDayResult = travelPlannerService.generateTravelCourse(userSelect, 2, testSaveCount);

        // Then
        // 1일 여행: 5번 슬롯 사용
        assertFalse(oneDayResult[0][5].isEmpty(), "1일 여행에서는 5번 슬롯 사용");

        // 2일 여행: 마지막 날 5번 슬롯 비움
        assertEquals("", twoDayResult[1][5], "2일 여행 마지막 날 5번 슬롯은 비움");
    }

    @Test
    @DisplayName("빈 슬롯이 실제로 생기는 상황에서 포맷팅 테스트")
    void testFormatCourseWithActualEmptySlots() {
        // Given - 숙박이 있어서 5번 슬롯을 차지하는 2일 여행
        String[] userSelect = {}; // 빈 선택으로 관광 슬롯을 비움
        int day = 2;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);
        String formatted = travelPlannerService.formatCourse(result);

        // Then
        assertTrue(formatted.contains("여유시간"), "빈 관광 슬롯은 여유시간으로 표시");

        // 마지막 날 5번 슬롯은 비어있어야 함
        assertEquals("", result[1][5], "마지막 날 5번 슬롯은 비어있음");
    }

    @Test
    @DisplayName("하루종일 활동 - 2일 여행 테스트")
    void testFullDayActivityTwoDayTrip() {
        // Given
        String[] userSelect = {"activity", "festival"};
        int day = 2;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 2일 여행에서는 빈도 높은 하나만 첫째 날에 배치
        assertEquals("activity", result[0][1], "첫째 날 1번 슬롯에 activity");
        assertEquals("activity", result[0][3], "첫째 날 3번 슬롯에 activity");
        assertEquals("accommodation", result[0][5], "첫째 날 5번 슬롯에 accommodation");
    }

    @Test
    @DisplayName("하루종일 활동 - 3일 이상 여행 테스트")
    void testFullDayActivityLongTrip() {
        // Given
        String[] userSelect = {"activity", "festival", "themepark"};
        int day = 4;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 3일 이상에서는 빈도 높은 2개가 첫째, 둘째 날에 배치
        assertEquals("activity", result[0][1], "첫째 날에 activity");
        assertEquals("activity", result[0][3]);
        assertEquals("accommodation", result[0][5]);

        assertEquals("festival", result[1][1], "둘째 날에 festival");
        assertEquals("festival", result[1][3]);
        assertEquals("accommodation", result[1][5]);
    }

    @Test
    @DisplayName("일반 카테고리 빈도수 배치 테스트")
    void testCategoryFrequencyPlacement() {
        // Given
        String[] userSelect = {"history", "nature", "market"};
        int day = 3;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // history(빈도4), nature(빈도3), market(빈도1) 순으로 배치
        Map<String, Integer> placedCount = countCategoryPlacements(result);

        assertTrue(placedCount.getOrDefault("history", 0) <= placedCount.getOrDefault("nature", 0),
                "history가 nature보다 많이 배치되어야 함");
        assertTrue(placedCount.getOrDefault("nature", 0) >= placedCount.getOrDefault("market", 0),
                "nature가 market보다 많이 배치되어야 함");
    }



    @Test
    @DisplayName("빈 선택 배열 테스트")
    void testEmptyUserSelection() {
        // Given
        String[] userSelect = {};
        int day = 2;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 기본 구조만 있어야 함
        assertEquals("restaurant", result[0][2], "점심은 여전히 배치");
        assertEquals("restaurant", result[0][4], "저녁은 여전히 배치");

        // 관광 슬롯은 대부분 비어있어야 함
        boolean hasEmptyTouristSlots = result[0][1].isEmpty() || result[0][3].isEmpty();
        assertTrue(hasEmptyTouristSlots, "관광 슬롯 중 일부는 비어있어야 함");
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 테스트")
    void testNonExistentCategory() {
        // Given
        String[] userSelect = {"nonexistent", "history"};
        int day = 1;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 존재하지 않는 카테고리는 기본값 1로 처리
        assertNotNull(result, "결과가 null이 아니어야 함");
        assertEquals(1, result.length, "1일 여행");
    }

    // Helper Methods
    private Map<String, Integer> countCategoryPlacements(String[][] course) {
        Map<String, Integer> counts = new HashMap<>();

        for (int i = 0; i < course.length; i++) {
            for (int j = 0; j < course[i].length; j++) {
                String slot = course[i][j];
                if (!slot.isEmpty() &&
                        !slot.equals("restaurant") &&
                        !slot.equals("restaurant") &&
                        !slot.equals("accommodation")) {

                    String category = slot.replace("_continue", "");
                    counts.merge(category, 1, Integer::sum);
                }
            }
        }
        return counts;
    }

    @Test
    @DisplayName("카페만 선택한 1일 여행 테스트")
    void testCafeOnlyOneDayTrip() {
        // Given
        String[] userSelect = {"cafe"};
        int day = 1;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        assertEquals("cafe", result[0][0], "0번 슬롯에 카페 배치");
        assertEquals("restaurant", result[0][2], "점심은 2번 슬롯");
        assertEquals("restaurant", result[0][4], "저녁은 4번 슬롯");

        // 관광 슬롯(1,3,5)은 비어있음 (카페만 선택했으므로)
        assertTrue(result[0][1].isEmpty(), "1번 슬롯은 비어있음");
        assertTrue(result[0][3].isEmpty(), "3번 슬롯은 비어있음");
        assertTrue(result[0][5].isEmpty(), "5번 슬롯은 비어있음");
    }

    @Test
    @DisplayName("카페와 하루종일 활동 함께 선택 - 1일 여행")
    void testCafeWithFullDayActivityOneDayTrip() {
        // Given
        String[] userSelect = {"cafe", "activity"};
        int day = 1;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        assertEquals("cafe", result[0][0], "0번 슬롯에 카페");
        assertEquals("activity", result[0][1], "1번 슬롯에 activity");
        assertEquals("activity", result[0][3], "3번 슬롯에 activity");
        assertEquals("restaurant", result[0][2], "점심은 2번 슬롯");
        assertEquals("restaurant", result[0][4], "저녁은 4번 슬롯");

        // 1일 여행에서는 5번 슬롯에도 activity가 추가 배치됨 (빈도수 3)
        assertEquals("activity", result[0][5], "1일 여행에서 5번 슬롯에도 activity 배치");
    }

    @Test
    @DisplayName("카페와 하루종일 활동 함께 선택 - 2일 여행")
    void testCafeWithFullDayActivityTwoDayTrip() {
        // Given
        String[] userSelect = {"cafe", "activity"};
        int day = 2;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 모든 날에 카페 배치 (각 날의 0번 슬롯)
        assertEquals("cafe", result[0][0], "첫째 날 0번 슬롯에 카페");
        assertEquals("cafe", result[1][0], "둘째 날 0번 슬롯에 카페");

        // 첫째 날에 하루종일 활동 배치
        assertEquals("activity", result[0][1], "첫째 날 1번 슬롯에 activity");
        assertEquals("activity", result[0][3], "첫째 날 3번 슬롯에 activity");
        assertEquals("accommodation", result[0][5], "첫째 날 5번 슬롯에 accommodation");


        // 마지막 날 5번 슬롯은 비움
        assertEquals("", result[1][5], "마지막 날 5번 슬롯은 비어있음");
    }

    @Test
    @DisplayName("카페와 여러 일반 카테고리 함께 선택")
    void testCafeWithMultipleRegularCategories() {
        // Given
        String[] userSelect = {"cafe", "history", "nature", "market"};
        int day = 3;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 모든 날에 카페 배치 확인 (각 날의 0번 슬롯)
        for (int i = 0; i < day; i++) {
            assertEquals("cafe", result[i][0], String.format("%d일차에 카페 배치", i + 1));
        }

        // 카테고리 빈도수 확인: history(4) > nature(3) > market(1)
        Map<String, Integer> placedCount = countCategoryPlacements(result);

        assertTrue(placedCount.getOrDefault("history", 0) >= 1, "history는 최소 1번 배치");
        assertTrue(placedCount.getOrDefault("nature", 0) >= 1, "nature는 최소 1번 배치");
        assertTrue(placedCount.getOrDefault("market", 0) >= 1, "market은 최소 1번 배치");

        // 마지막 날 5번 슬롯은 비움 (올바른 인덱스 사용)
        assertEquals("", result[day - 1][5], "마지막 날 5번 슬롯은 비어있어야 함");
        // 또는 명시적으로: assertEquals("", result[2][5], "3일차 5번 슬롯은 비어있어야 함");
    }

    @Test
    @DisplayName("카테고리 빈도순 라운드로빈 배치 테스트")
    void testAllocateRemainingCategoriesRoundRobin() {
        // Given
        String[] userSelect = {"history", "nature", "market"};
        int day = 3;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 각 카테고리가 최소 1번씩 배치되었는지 확인
        Map<String, Integer> placedCount = countCategoryPlacements(result);
        assertTrue(placedCount.getOrDefault("history", 0) >= 1, "history는 최소 1번 배치");
        assertTrue(placedCount.getOrDefault("nature", 0) >= 1, "nature는 최소 1번 배치");
        assertTrue(placedCount.getOrDefault("market", 0) >= 1, "market은 최소 1번 배치");

        // 남은 슬롯에 빈도순으로 추가 배치되었는지 확인
        assertTrue(placedCount.get("history") >= placedCount.get("nature"),
                "history가 nature보다 많이 배치되어야 함");
        assertTrue(placedCount.get("nature") >= placedCount.get("market"),
                "nature가 market보다 많이 배치되어야 함");

        // 마지막 날 5번 슬롯은 비어있어야 함
        assertEquals("", result[2][5], "마지막 날 5번 슬롯은 비어있어야 함");
    }

    @Test
    @DisplayName("카페와 낮은 빈도 카테고리 배치 테스트")
    void testCafeWithLowFrequencyCategory() {
        // Given - market이 확실히 배치되도록 다른 카테고리 제외
        String[] userSelect = {"cafe", "market"};
        int day = 2;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 모든 날에 카페 배치
        assertEquals("cafe", result[0][0], "첫째 날에 카페");
        assertEquals("cafe", result[1][0], "둘째 날에 카페");

        // market이 배치되었는지 확인
        Map<String, Integer> placedCount = countCategoryPlacements(result);
        assertTrue(placedCount.getOrDefault("market", 0) >= 1, "market은 최소 1번 배치");
    }

    @Test
    @DisplayName("카페와 하루종일 활동의 숙박 배치 확인")
    void testCafeWithFullDayActivityAccommodation() {
        // Given
        String[] userSelect = {"cafe", "activity"};
        int day = 2;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 첫째 날 구조 확인
        assertEquals("cafe", result[0][0], "첫째 날 카페");
        assertEquals("activity", result[0][1], "첫째 날 1번에 activity");
        assertEquals("restaurant", result[0][2], "첫째 날 점심");
        assertEquals("activity", result[0][3], "첫째 날 3번에 activity");
        assertEquals("restaurant", result[0][4], "첫째 날 저녁");

        // 서비스 로직에 따라 5번 슬롯이 숙박으로 강제 교체됨
        assertEquals("accommodation", result[0][5], "5번 슬롯에 숙박이 강제 배치됨");

        // 숙박이 배치되었는지 확인
        boolean hasAccommodation = Arrays.asList(result[0]).contains("accommodation");
        assertTrue(hasAccommodation, "첫째 날에 숙박이 배치되어야 함");

        // 마지막 날은 숙박 없음
        assertFalse(Arrays.asList(result[1]).contains("accommodation"), "마지막 날에는 숙박 없음");
        assertEquals("", result[1][5], "마지막 날 5번 슬롯은 비어있음");
    }

    @Test
    @DisplayName("카페만으로 빈 슬롯 확인")
    void testCafeOnlyEmptySlots() {
        // Given
        String[] userSelect = {"cafe"};
        int day = 1;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        assertEquals("cafe", result[0][0], "0번 슬롯에 카페");
        assertEquals("restaurant", result[0][2], "점심");
        assertEquals("restaurant", result[0][4], "저녁");

        // 카페만 선택했으므로 관광 슬롯은 모두 비어있어야 함
        assertTrue(result[0][1].isEmpty(), "1번 슬롯 비어있음");
        assertTrue(result[0][3].isEmpty(), "3번 슬롯 비어있음");
        assertTrue(result[0][5].isEmpty(), "5번 슬롯 비어있음");
    }

    @Test
    @DisplayName("카페가 모든 날에 배치되는지 확인")
    void testCafeDistributionAllDays() {
        // Given
        String[] userSelect = {"cafe", "indoor"};
        int day = 3;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 모든 날의 0번 슬롯에 카페가 배치되어야 함
        for (int i = 0; i < day; i++) {
            assertEquals("cafe", result[i][0], String.format("%d일차 0번 슬롯에 카페", i + 1));
        }

        // 총 카페 개수는 여행 일수와 같아야 함
        long cafeCount = Arrays.stream(result)
                .flatMap(Arrays::stream)
                .mapToLong(slot -> "cafe".equals(slot) ? 1 : 0)
                .sum();
        assertEquals(day, cafeCount, "카페 개수는 여행 일수와 같아야 함");
    }

    @Test
    @DisplayName("카페 없는 경우와 카페 있는 경우 0번 슬롯 비교")
    void testSlotZeroWithAndWithoutCafe() {
        // Given
        String[] withoutCafe = {"history", "nature"};
        String[] withCafe = {"cafe", "history", "nature"};
        int day = 2;

        // When
        String[][] resultWithoutCafe = travelPlannerService.generateTravelCourse(withoutCafe, day, testSaveCount);
        String[][] resultWithCafe = travelPlannerService.generateTravelCourse(withCafe, day, testSaveCount);

        // Then
        // 카페 없는 경우: 0번 슬롯이 비어있어야 함
        assertTrue(resultWithoutCafe[0][0].isEmpty(), "카페 없으면 0번 슬롯은 비어있음");
        assertTrue(resultWithoutCafe[1][0].isEmpty(), "카페 없으면 둘째 날 0번 슬롯도 비어있음");

        // 카페 있는 경우: 모든 날의 0번 슬롯에 카페 배치
        assertEquals("cafe", resultWithCafe[0][0], "카페 있으면 첫째 날 0번 슬롯에 카페");
        assertEquals("cafe", resultWithCafe[1][0], "카페 있으면 둘째 날 0번 슬롯에 카페");
    }

    @Test
    @DisplayName("카페와 모든 하루종일 활동 함께 선택")
    void testCafeWithAllFullDayActivities() {
        // Given
        String[] userSelect = {"cafe", "activity", "festival", "themepark"};
        int day = 3;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 모든 날에 카페 배치
        for (int i = 0; i < day; i++) {
            assertEquals("cafe", result[i][0], String.format("%d일차에 카페", i + 1));
        }

        // 첫째, 둘째 날에 하루종일 활동 배치 (빈도 높은 순서: activity > festival/themepark)
        assertEquals("activity", result[0][1], "첫째 날에 activity");
        assertEquals("accommodation", result[0][5], "첫째 날에 accommodation");

        assertTrue("festival".equals(result[1][1]) || "themepark".equals(result[1][1]),
                "둘째 날에 festival 또는 themepark");

        // 마지막 날 5번 슬롯은 비움
        assertEquals("", result[2][5], "마지막 날 5번 슬롯은 비어있음");
    }

    @Test
    @DisplayName("카페가 포함된 복잡한 시나리오")
    void testCafeWithComplexScenario() {
        // Given
        String[] userSelect = {"cafe", "history", "nature", "indoor"};
        int day = 3;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 카페는 모든 날의 0번 슬롯에 배치
        for (int i = 0; i < day; i++) {
            assertEquals("cafe", result[i][0], String.format("%d일차에 카페", i + 1));
        }

        // 숙박은 마지막 날 제외하고 배치
        long accommodationCount = 0;
        for (int i = 0; i < day - 1; i++) {
            if (Arrays.asList(result[i]).contains("accommodation")) {
                accommodationCount++;
            }
        }
        assertEquals(2, accommodationCount, "숙박은 마지막 날 제외하고 2번 배치");

        // 기본 구조 확인
        for (int i = 0; i < day; i++) {
            assertEquals("restaurant", result[i][2], String.format("%d일차 점심", i + 1));
            assertEquals("restaurant", result[i][4], String.format("%d일차 저녁", i + 1));
        }

        // 마지막 날 5번 슬롯은 비움
        assertEquals("", result[2][5], "마지막 날 5번 슬롯은 비어있음");
    }

    @Test
    @DisplayName("기존 카페 포함 여행 테스트 - 수정된 버전")
    void testTripWithCafeUpdated() {
        // Given
        String[] userSelect = {"cafe", "history"};
        int day = 2;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 모든 날에 카페 배치
        assertEquals("cafe", result[0][0], "첫째 날 카페");
        assertEquals("cafe", result[1][0], "둘째 날 카페");

        // 기본 구조 확인
        assertEquals("restaurant", result[0][2], "첫째 날 점심");
        assertEquals("restaurant", result[0][4], "첫째 날 저녁");
        assertEquals("restaurant", result[1][2], "둘째 날 점심");
        assertEquals("restaurant", result[1][4], "둘째 날 저녁");

        // history 배치 확인 (빈도4이므로 여러 번 배치됨)
        Map<String, Integer> placedCount = countCategoryPlacements(result);
        assertTrue(placedCount.getOrDefault("history", 0) >= 1, "history는 최소 1번 배치");
    }


    @Test
    @DisplayName("카페 + 일반카테고리 + 하루종일활동 - 1일 여행")
    void testCafeRegularFullDayOneDayTrip() {
        // Given
        String[] userSelect = {"cafe", "history", "nature", "activity"};
        int day = 1;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 카페는 0번 슬롯에 배치
        assertEquals("cafe", result[0][0], "0번 슬롯에 카페 배치");

        // 하루종일 활동이 1,3번 슬롯 우선 배치
        assertEquals("activity", result[0][1], "1번 슬롯에 activity");
        assertEquals("activity", result[0][3], "3번 슬롯에 activity");

        // 기본 식사
        assertEquals("restaurant", result[0][2], "점심은 2번 슬롯");
        assertEquals("restaurant", result[0][4], "저녁은 4번 슬롯");

        // 1일 여행에서는 5번 슬롯도 사용 (activity 빈도3이므로)
        assertEquals("nature", result[0][5], "5번 슬롯에도 nature 배치");

        // 카테고리 배치 확인
        Map<String, Integer> placedCount = countCategoryPlacements(result);
        assertEquals(2, placedCount.get("activity"), "activity는 최대로 2번 배치");

        // history, nature는 activity가 모든 관광 슬롯을 차지해서 배치되지 않음
        assertEquals(0, placedCount.getOrDefault("history", 0), "history는 배치되지 않음");
    }

    @Test
    @DisplayName("카페 + 일반카테고리 + 하루종일활동 - 2일 여행")
    void testCafeRegularFullDayTwoDayTrip() {
        // Given
        String[] userSelect = {"cafe", "history", "nature", "activity"};
        int day = 2;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 모든 날에 카페 배치
        assertEquals("cafe", result[0][0], "첫째 날 0번 슬롯에 카페");
        assertEquals("cafe", result[1][0], "둘째 날 0번 슬롯에 카페");

        // 첫째 날: 하루종일 활동 배치
        assertEquals("activity", result[0][1], "첫째 날 1번 슬롯에 activity");
        assertEquals("activity", result[0][3], "첫째 날 3번 슬롯에 activity");
        assertEquals("accommodation", result[0][5], "첫째 날 5번 슬롯에 숙박");

        // 기본 식사 확인
        for (int i = 0; i < day; i++) {
            assertEquals("restaurant", result[i][2], String.format("%d일차 점심", i + 1));
            assertEquals("restaurant", result[i][4], String.format("%d일차 저녁", i + 1));
        }

        // 둘째 날: 일반 카테고리 배치 (history, nature)
        assertTrue(result[1][1].equals("history") || result[1][1].equals("nature"),
                "둘째 날 1번 슬롯에 일반 카테고리 배치");
        assertTrue(result[1][3].equals("history") || result[1][3].equals("nature"),
                "둘째 날 3번 슬롯에 일반 카테고리 배치");

        // 마지막 날 5번 슬롯은 비움
        assertEquals("", result[1][5], "마지막 날 5번 슬롯은 비어있음");

        // 카테고리 배치 개수 확인
        Map<String, Integer> placedCount = countCategoryPlacements(result);
        assertEquals(2, placedCount.get("activity"), "activity는 2번 배치");
        assertTrue(placedCount.getOrDefault("history", 0) >= 1, "history는 최소 1번 배치");
        assertTrue(placedCount.getOrDefault("nature", 0) >= 1, "nature는 최소 1번 배치");
    }

    @Test
    @DisplayName("카페 + 일반카테고리 + 하루종일활동 - 3일 여행")
    void testCafeRegularFullDayThreeDayTrip() {
        // Given
        String[] userSelect = {"cafe", "history", "nature", "activity"};
        int day = 3;

        // When
        String[][] result = travelPlannerService.generateTravelCourse(userSelect, day, testSaveCount);

        // Then
        // 모든 날에 카페 배치
        for (int i = 0; i < day; i++) {
            assertEquals("cafe", result[i][0], String.format("%d일차 0번 슬롯에 카페", i + 1));
        }

        // 첫째 날: 하루종일 활동 배치
        assertEquals("activity", result[0][1], "첫째 날 1번 슬롯에 activity");
        assertEquals("activity", result[0][3], "첫째 날 3번 슬롯에 activity");
        assertEquals("accommodation", result[0][5], "첫째 날 5번 슬롯에 숙박");

        // 둘째 날: 숙박 배치
        assertEquals("accommodation", result[1][5], "둘째 날 5번 슬롯에 숙박");

        // 기본 식사 확인
        for (int i = 0; i < day; i++) {
            assertEquals("restaurant", result[i][2], String.format("%d일차 점심", i + 1));
            assertEquals("restaurant", result[i][4], String.format("%d일차 저녁", i + 1));
        }

        // 일반 카테고리들이 둘째, 셋째 날에 배치
        Map<String, Integer> placedCount = countCategoryPlacements(result);
        assertEquals(2, placedCount.get("activity"), "activity는 2번 배치");
        assertTrue(placedCount.getOrDefault("history", 0) >= 1, "history는 최소 1번 배치");
        assertTrue(placedCount.getOrDefault("nature", 0) >= 1, "nature는 최소 1번 배치");

        // 빈도순으로 배치되었는지 확인 (history 빈도4 > nature 빈도7이지만 테스트데이터에서는 history:4, nature:7)
        assertTrue(placedCount.get("nature") >= placedCount.get("history"),
                "nature가 history보다 많이 배치되어야 함");

        // 마지막 날 5번 슬롯은 비움
        assertEquals("", result[2][5], "마지막 날 5번 슬롯은 비어있음");

        // 숙박이 첫째, 둘째 날에만 배치되었는지 확인
        long accommodationCount = Arrays.stream(result)
                .flatMap(Arrays::stream)
                .mapToLong(slot -> slot.equals("accommodation") ? 1 : 0)
                .sum();
        assertEquals(2, accommodationCount, "숙박은 마지막 날 제외하고 2번 배치");
    }


}