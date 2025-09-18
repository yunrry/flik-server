package yunrry.flik.core.domain.model;

public enum SubCategory {
    // 51개
    // Nature
    MOUNTAIN_SCENERY("NA01", "자연경관(산)"),
    WATER_SCENERY("NA02", "자연경관(하천‧해양)"),
    NATURAL_ECOLOGY("NA03", "자연생태"),
    NATURAL_PARK("NA04", "자연공원"),
    TEMPLE_EXPERIENCE("EX04", "산사체험"),
    OTHER_NATURE("NA06", "기타자연관광"),

    // Indoor
    EXHIBITION_FACILITY("VE07", "전시시설"),
    PERFORMANCE_FACILITY("VE06", "공연시설"),
    EDUCATION_FACILITY("VE09", "교육시설"),
    SHOPPING_MALL("SH02", "쇼핑몰"),
    DEPARTMENT_STORE("SH01", "백화점"),
    LARGE_MART("SH03", "대형마트"),

    // History Culture
    HISTORIC_SITE("HS01", "역사유적지"),
    HISTORIC_RELIC("HS02", "역사유물"),
    RELIGIOUS_SITE("HS03", "종교성지"),
    SECURITY_TOURISM("HS04", "안보관광지"),
    OTHER_CULTURAL_SITE("VE12", "기타문화관광지"),
    LANDMARK_TOURISM("VE01", "랜드마크관광"),
    CITY_PARK("VE03", "도시공원"),
    CITY_REGIONAL_CULTURE("VE04", "도시.지역문화관광"),
    INDUSTRIAL_TOURISM("EX06", "산업관광"),

    // Cafe
    CAFE_TEA_HOUSE("FD05", "카페/ 찻집"),

    // Activity
    LAND_LEISURE_SPORTS("LS01", "육상레저스포츠"),
    WATER_LEISURE_SPORTS("LS02", "수상레저스포츠"),
    AVIATION_LEISURE_SPORTS("LS03", "항공레저스포츠"),
    TRADITIONAL_EXPERIENCE("EX01", "전통체험"),
    CRAFT_EXPERIENCE("EX02", "공예체험"),
    FARM_VILLAGE_EXPERIENCE("EX03", "농.산.어촌체험"),
    WELLNESS_TOURISM("EX05", "웰니스관광"),
    COMPLEX_LEISURE_SPORTS("LS04", "복합레저스포츠"),
    LEISURE_SPORTS_FACILITY("VE10", "레저스포츠시설"),
    OTHER_EXPERIENCE("EX07", "기타체험"),

    // Festival
    FESTIVAL("EV01", "축제"),
    PERFORMANCE("EV02", "공연"),
    EVENT("EV03", "행사"),

    // Market
    MARKET("SH06", "시장"),
    SPECIALTY_STORE("SH05", "전문매장/상가"),
    DUTY_FREE_SHOP("SH04", "면세점"),
    OTHER_SHOPPING_FACILITY("SH07", "기타쇼핑시설"),


    // Theme Park
    THEME_PARK("VE02", "테마공원"),
    COMPLEX_TOURISM_FACILITY("VE05", "복합관광시설"),

    // Restaurant
    KOREAN_FOOD("FD01", "한식"),
    FOREIGN_FOOD("FD02", "외국식"),
    SIMPLE_FOOD("FD03", "간이음식"),
    PUB_BAR("FD04", "주점"),

    // Accommodation
    HOTEL("AC01", "호텔"),
    CONDOMINIUM("AC02", "콘도미니엄"),
    PENSION_HOMESTAY("AC03", "펜션/민박"),
    MOTEL("AC04", "모텔"),
    CAMPING("AC05", "캠핑"),
    HOSTEL("AC06", "호스텔");

    private final String code;
    private final String koreanName;

    SubCategory(String code, String koreanName) {
        this.code = code;
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public String getCode() {
        return code;
    }

    /**
     * 한국어 이름으로 SubCategory 찾기
     */
    public static SubCategory findByKoreanName(String koreanName) {
        for (SubCategory category : values()) {
            if (category.getKoreanName().equals(koreanName)) {
                return category;
            }
        }
        return null;
    }

    public static SubCategory findByCode(String code) {
        for (SubCategory category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        return null;
    }

    public static String findKoreanNameByCode(String code) {
        for (SubCategory category : values()) {
            if (category.getCode().equals(code)) {
                return category.getKoreanName();
            }
        }
        return null;
    }
}