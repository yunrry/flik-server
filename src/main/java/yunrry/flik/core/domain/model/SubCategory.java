package yunrry.flik.core.domain.model;

public enum SubCategory {

    // Nature
    MOUNTAIN_SCENERY("자연경관(산)"),
    WATER_SCENERY("자연경관(하천‧해양)"),
    NATURAL_ECOLOGY("자연생태"),
    NATURAL_PARK("자연공원"),
    TEMPLE_EXPERIENCE("산사체험"),
    OTHER_NATURE("기타자연관광"),

    // Indoor
    EXHIBITION_FACILITY("전시시설"),
    PERFORMANCE_FACILITY("공연시설"),
    EDUCATION_FACILITY("교육시설"),
    SHOPPING_MALL("쇼핑몰"),
    DEPARTMENT_STORE("백화점"),
    LARGE_MART("대형마트"),

    // History Culture
    HISTORIC_SITE("역사유적지"),
    HISTORIC_RELIC("역사유물"),
    RELIGIOUS_SITE("종교성지"),
    SECURITY_TOURISM("안보관광지"),
    OTHER_CULTURAL_SITE("기타문화관광지"),
    LANDMARK_TOURISM("랜드마크관광"),
    CITY_PARK("도시공원"),
    CITY_REGIONAL_CULTURE("도시.지역문화관광"),
    INDUSTRIAL_TOURISM("산업관광"),

    // Cafe
    CAFE_TEA_HOUSE("카페/찻집"),

    // Activity
    LAND_LEISURE_SPORTS("육상레저스포츠"),
    WATER_LEISURE_SPORTS("수상레저스포츠"),
    AVIATION_LEISURE_SPORTS("항공레저스포츠"),
    TRADITIONAL_EXPERIENCE("전통체험"),
    CRAFT_EXPERIENCE("공예체험"),
    FARM_VILLAGE_EXPERIENCE("농.산.어촌체험"),
    WELLNESS_TOURISM("웰니스관광"),
    COMPLEX_LEISURE_SPORTS("복합레저스포츠"),
    LEISURE_SPORTS_FACILITY("레저스포츠시설"),
    OTHER_EXPERIENCE("기타체험"),

    // Festival
    FESTIVAL("축제"),
    PERFORMANCE("공연"),
    EVENT("행사"),

    // Market
    MARKET("시장"),
    SPECIALTY_STORE("전문매장/상가"),
    DUTY_FREE_SHOP("면세점"),
    OTHER_SHOPPING_FACILITY("기타쇼핑시설"),

    // Theme Park
    THEME_PARK("테마공원"),
    COMPLEX_TOURISM_FACILITY("복합관광시설"),

    // Restaurant
    KOREAN_FOOD("한식"),
    FOREIGN_FOOD("외국식"),
    SIMPLE_FOOD("간이음식"),
    PUB_BAR("주점"),

    // Accommodation
    HOTEL("호텔"),
    CONDOMINIUM("콘도미니엄"),
    PENSION_HOMESTAY("펜션/민박"),
    MOTEL("모텔"),
    CAMPING("캠핑"),
    HOSTEL("호스텔");

    private final String koreanName;

    SubCategory(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
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
}