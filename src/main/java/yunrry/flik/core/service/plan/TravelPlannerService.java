package yunrry.flik.core.service.plan;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TravelPlannerService {

    // KEY_LIST = {"nature", "indoor", "history", "cafe", "activity", "festival", "market", "themepark"};
    private static final int MAX_PLACES_PER_DAY = 6;

    /**
     * 여행 코스를 생성합니다.
     * @param userSelect 사용자가 선택한 카테고리 배열
     * @param day 여행 일수
     * @param saveCount 각 카테고리별 저장 빈도수 맵
     * @return 생성된 여행 코스 (일차별 6개 슬롯)
     */
    public String[][] generateTravelCourse(String[] userSelect, int day, Map<String, Integer> saveCount) {

        if(saveCount.size()<2){
            throw new IllegalArgumentException("최소 2개 카테고리 필요");
        }
        if(saveCount.size()>4){
            throw new IllegalArgumentException("최대 4개 카테고리 제한");
        }

        // 1. 초기화
        Map<String, Integer> selected = new HashMap<>();
        String[][] course = new String[day][MAX_PLACES_PER_DAY];

        for (String category : userSelect) {
            selected.put(category, saveCount.getOrDefault(category, 1));
        }

        for (int i = 0; i < day; i++) {
            Arrays.fill(course[i], "");
        }

        // 2. 기본 필수 요소 배치
        allocateBasicElements(course, day, selected.containsKey("cafe"));

        // 3. 카페 제거 (이미 배치됨)
        selected.remove("cafe");

        // 4. 모든 카테고리 통합 배치 (특별 카테고리 포함)
        allocateAllCategories(course, selected, day);

        // 5. 마지막 날 마지막 슬롯 비우기
        if (day > 1) {
            course[day - 1][5] = "";
        }

        return course;
    }

    private void allocateBasicElements(String[][] course, int day, boolean hasCafe) {
        for (int i = 0; i < day; i++) {
            // 식사 배치 (점심, 저녁)
            course[i][2] = "restaurant";
            course[i][4] = "restaurant";

            // 카페 배치 (있는 경우) - 0번 슬롯
            if (hasCafe) {
                course[i][0] = "cafe";
            }

            // 숙박 배치 (1박 이상인 경우, 마지막 날 제외)
            if (day > 1 && i < day - 1) {
                addAccommodation(course[i]);
            }
        }
    }

    private void addAccommodation(String[] daySchedule) {
        // 빈 슬롯을 찾아서 숙박 배치 (우선순위: 5번 -> 3번 -> 1번)
        if (daySchedule[5].isEmpty()) {
            daySchedule[5] = "accommodation";
        } else {
            daySchedule[3] = "accommodation";
        }
    }


    /**
     * 모든 카테고리를 빈도순으로 배치 (카페 제외)
     * 특별 카테고리도 일반 카테고리와 동일하게 처리
     */
    private void allocateAllCategories(String[][] course, Map<String, Integer> selected, int day) {
        // 빈도순 정렬
        List<Map.Entry<String, Integer>> sortedEntries = selected.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        int dayIndex = 0;
        Map<String, Integer> remainingCount = new HashMap<>();

        for (Map.Entry<String, Integer> entry : sortedEntries) {
            remainingCount.put(entry.getKey(), entry.getValue());
        }

        // 라운드로빈 배치
        while (!remainingCount.isEmpty()) {
            boolean placed = false;

            for (Map.Entry<String, Integer> entry : sortedEntries) {
                String category = entry.getKey();

                if (!remainingCount.containsKey(category)) {
                    continue;
                }

                // 배치 가능한 날 찾기
                boolean placedThisRound = false;
                for (int attempts = 0; attempts < day; attempts++) {
                    int currentDay = (dayIndex + attempts) % day;

                    if (hasEmptyTouristSlot(course[currentDay])) {
                        placeCategoryInDay(course[currentDay], category);
                        dayIndex = (currentDay + 1) % day;
                        placed = true;
                        placedThisRound = true;
                        break;
                    }
                }

                if (placedThisRound) {
                    int remaining = remainingCount.get(category) - 1;
                    if (remaining <= 0) {
                        remainingCount.remove(category);
                    } else {
                        remainingCount.put(category, remaining);
                    }
                }
            }

            if (!placed) {
                break;
            }
        }
    }

    private boolean hasEmptyTouristSlot(String[] daySchedule) {
        boolean slot5Available = daySchedule[5].isEmpty() && !daySchedule[5].equals("accommodation");
        return daySchedule[1].isEmpty() || daySchedule[3].isEmpty() || slot5Available;
    }

    private void placeCategoryInDay(String[] daySchedule, String category) {
        if (daySchedule[1].isEmpty()) {
            daySchedule[1] = category;
        } else if (daySchedule[3].isEmpty()) {
            daySchedule[3] = category;
        } else if (daySchedule[5].isEmpty() && !daySchedule[5].equals("accommodation")) {
            daySchedule[5] = category;
        }
    }




    /**
     * 여행 코스를 보기 좋게 포맷팅합니다.
     * @param course 생성된 여행 코스
     * @return 포맷팅된 문자열
     */
    public String formatCourse(String[][] course) {
        StringBuilder sb = new StringBuilder();
        String[] slotNames = {"카페", "관광1", "점심", "관광2", "저녁", "관광3"};

        for (int day = 0; day < course.length; day++) {
            sb.append(String.format("=== %d일차 ===\n", day + 1));
            for (int slot = 0; slot < course[day].length; slot++) {
                String activity = course[day][slot].isEmpty() ? "여유시간" : course[day][slot];
                sb.append(String.format("%s: %s\n", slotNames[slot], activity));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}