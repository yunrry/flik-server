package yunrry.flik.core.service.plan;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TravelPlannerService {

    private static final String[] KEY_LIST = {"nature", "indoor", "history", "cafe", "activity", "festival", "market", "themepark"};
    private static final int MAX_PLACES_PER_DAY = 6;

    /**
     * 여행 코스를 생성합니다.
     * @param userSelect 사용자가 선택한 카테고리 배열
     * @param day 여행 일수
     * @param saveCount 각 카테고리별 저장 빈도수 맵
     * @return 생성된 여행 코스 (일차별 6개 슬롯)
     */
    public String[][] generateTravelCourse(String[] userSelect, int day, Map<String, Integer> saveCount) {

        List<String> filteredCategories = Arrays.stream(userSelect)
                .filter(cat -> !cat.equalsIgnoreCase("restaurant") && !cat.equalsIgnoreCase("accommodation"))
                .collect(Collectors.toList());


        // 1. 초기화
        Map<String, Integer> selected = new HashMap<>();
        String[][] course = new String[day][MAX_PLACES_PER_DAY];

        // 사용자 선택 카테고리 빈도수 설정
        for (String category : userSelect) {
            selected.put(category, saveCount.getOrDefault(category, 1));
        }

        // 코스 배열 초기화
        for (int i = 0; i < day; i++) {
            Arrays.fill(course[i], "");
        }

        // 2. 기본 필수 요소 배치
        allocateBasicElements(course, day, selected.containsKey("cafe"));

        // 3. 특별 카테고리 처리 (하루 종일 소요)
        handleFullDayActivities(course, selected, day);

        // 4. 나머지 카테고리 배치
        allocateRemainingCategories(course, selected, day);

        // 5. 마지막 날 마지막 슬롯 비우기 (2일 이상 여행만)
        if (day > 1) {
            setLastDayEmpty(course, day);
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

    private void handleFullDayActivities(String[][] course, Map<String, Integer> selected, int day) {

        List<String> fullDayActivities = Arrays.asList("activity", "festival", "themepark");
        List<Map.Entry<String, Integer>> availableFullDay = new ArrayList<>();

        // 하루 종일 소요 활동을 빈도순으로 찾기
        for (String activity : fullDayActivities) {
            if (selected.containsKey(activity)) {
                availableFullDay.add(new AbstractMap.SimpleEntry<>(activity, selected.get(activity)));
            }
        }

        // 빈도순으로 정렬 (높은 순)
        availableFullDay.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

        int fullDayCount = 0;

        if (day == 1) {
            if (!availableFullDay.isEmpty()) {
                String topActivity = availableFullDay.get(0).getKey();
                course[0][1] = topActivity;
                course[0][3] = topActivity;
            }
            return;
        } else if (day == 2) {
            fullDayCount = Math.min(1, availableFullDay.size());
        } else if (day >= 3) {
            if (availableFullDay.size() >= 2) {
                fullDayCount = 2;
            } else if (availableFullDay.size() == 1) {
                fullDayCount = 1;
            }
        }

        // 실제 하루 종일 활동 배치 (2일 이상인 경우)
        for (int i = 0; i < fullDayCount && i < availableFullDay.size(); i++) {
            String fullDayActivity = availableFullDay.get(i).getKey();

            course[i][1] = fullDayActivity;
            course[i][3] = fullDayActivity;

            // 5번 슬롯에 숙박이 없는 경우에만 activity_continue 배치
            if (!course[i][5].equals("accommodation")) {
                course[i][5] = fullDayActivity + "_continue";
            }

            selected.remove(fullDayActivity);
        }
    }

    private void redistributeCafe(String[][] course, int day, int excludeDay) {
        for (int i = 0; i < day; i++) {
            if (i != excludeDay && hasEmptySlot(course[i])) {
                // 빈 슬롯에 카페 배치
                for (int j = 0; j < MAX_PLACES_PER_DAY; j++) {
                    if (course[i][j].isEmpty()) {
                        course[i][j] = "cafe";
                        return;
                    }
                }
            }
        }
    }

    private void allocateRemainingCategories(String[][] course, Map<String, Integer> selected, int day) {

        // 카페 제거 (이미 처리됨)
        selected.remove("cafe");

        // 빈도수 기준으로 정렬
        List<Map.Entry<String, Integer>> sortedEntries = selected.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        int dayIndex = 0;

        // 각 카테고리별 남은 배치 횟수 추적
        Map<String, Integer> remainingCount = new HashMap<>();
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            remainingCount.put(entry.getKey(), entry.getValue());
        }

        // 모든 카테고리가 배치될 때까지 라운드로빈 반복
        while (!remainingCount.isEmpty()) {
            boolean placed = false;

            // 빈도순으로 정렬된 카테고리들을 순회하며 1개씩 배치
            Iterator<Map.Entry<String, Integer>> iterator = sortedEntries.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Integer> entry = iterator.next();
                String category = entry.getKey();

                // 이미 모두 배치된 카테고리는 건너뛰기
                if (!remainingCount.containsKey(category)) {
                    continue;
                }

                // 배치 가능한 날 찾기
                boolean placedThisRound = false;
                for (int attempts = 0; attempts < day; attempts++) {
                    int currentDay = (dayIndex + attempts) % day;

                    if (hasEmptyTouristSlot(course[currentDay])) {
                        placeCategoryInDay(course[currentDay], category);
                        dayIndex = (currentDay + 1) % day; // 다음 날로 이동
                        placed = true;
                        placedThisRound = true;
                        break;
                    }
                }

                if (placedThisRound) {
                    // 남은 횟수 감소
                    int remaining = remainingCount.get(category) - 1;
                    if (remaining <= 0) {
                        remainingCount.remove(category);
                    } else {
                        remainingCount.put(category, remaining);
                    }
                }
            }

            // 더 이상 배치할 수 없으면 종료
            if (!placed) {
                break;
            }
        }
    }

    private boolean hasEmptySlot(String[] daySchedule) {
        for (String slot : daySchedule) {
            if (slot.isEmpty()) return true;
        }
        return false;
    }

    private boolean hasEmptyTouristSlot(String[] daySchedule) {
        // 5번 슬롯이 accommodation인 경우 해당 슬롯은 사용할 수 없음
        boolean slot5Available = daySchedule[5].isEmpty() && !daySchedule[5].equals("accommodation");
        return daySchedule[1].isEmpty() || daySchedule[3].isEmpty() || slot5Available;
    }

    private void placeCategoryInDay(String[] daySchedule, String category) {
        // 우선순위: 1번 슬롯(관광1) -> 3번 슬롯(관광2) -> 5번 슬롯(관광3)
        if (daySchedule[1].isEmpty()) {
            daySchedule[1] = category;
        } else if (daySchedule[3].isEmpty()) {
            daySchedule[3] = category;
        } else if (daySchedule[5].isEmpty()) {
            daySchedule[5] = category;
        }
        // accommodation이 있는 5번 슬롯은 건드리지 않음
    }

    private void setLastDayEmpty(String[][] course, int day) {
        // 마지막 날의 마지막 슬롯(5번)을 공백으로 설정 (2일 이상 여행만)
        if (day > 1) {
            course[day - 1][5] = ""; // 마지막 날 5번 슬롯 비우기
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