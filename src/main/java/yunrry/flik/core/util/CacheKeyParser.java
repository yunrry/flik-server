package yunrry.flik.core.util;

import yunrry.flik.core.domain.model.MainCategory;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CacheKeyParser {

    /**
     * 캐시 키에서 카테고리 목록 추출
     * 예: "ATTRACTION_FOOD:11:21:3" -> [ATTRACTION, FOOD]
     */
    public static List<MainCategory> extractCategories(String cacheKey) {
        String categoriesStr = cacheKey.split(":")[0];
        return Arrays.stream(categoriesStr.split("_"))
                .map(MainCategory::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * 캐시 키에서 지역 코드 추출
     * 예: "ATTRACTION_FOOD:11:21:3" -> "11"
     */
    public static String extractRegionCode(String cacheKey) {
        return cacheKey.split(":")[1];
    }

    /**
     * 캐시 키에서 카테고리별 제한 개수 추출
     * 예: "ATTRACTION_FOOD:11:21:3" -> 21
     */
    public static int extractLimitPerCategory(String cacheKey) {
        return Integer.parseInt(cacheKey.split(":")[2]);
    }

    /**
     * 캐시 키에서 여행 기간 추출
     * 예: "ATTRACTION_FOOD:11:21:3" -> 3
     */
    public static int extractTripDuration(String cacheKey) {
        return Integer.parseInt(cacheKey.split(":")[3]);
    }

    /**
     * 캐시 키 파싱 결과를 담는 클래스
     */
    public static class ParsedCacheKey {
        private final List<MainCategory> categories;
        private final String regionCode;
        private final int limitPerCategory;
        private final int tripDuration;

        public ParsedCacheKey(List<MainCategory> categories, String regionCode, int limitPerCategory, int tripDuration) {
            this.categories = categories;
            this.regionCode = regionCode;
            this.limitPerCategory = limitPerCategory;
            this.tripDuration = tripDuration;
        }

        public List<MainCategory> getCategories() { return categories; }
        public String getRegionCode() { return regionCode; }
        public int getLimitPerCategory() { return limitPerCategory; }
        public int getTripDuration() { return tripDuration; }
    }

    /**
     * 캐시 키를 한 번에 파싱
     */
    public static ParsedCacheKey parse(String cacheKey) {
        List<MainCategory> categories = extractCategories(cacheKey);
        String regionCode = extractRegionCode(cacheKey);
        int limitPerCategory = extractLimitPerCategory(cacheKey);
        int tripDuration = extractTripDuration(cacheKey);

        return new ParsedCacheKey(categories, regionCode, limitPerCategory, tripDuration);
    }
}