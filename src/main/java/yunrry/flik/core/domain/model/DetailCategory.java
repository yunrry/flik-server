package yunrry.flik.core.domain.model;

public enum DetailCategory {

    // 레저스포츠 - 육상레저스포츠 LS01
    ATV("ATV"),
    MTB("MTB"),
    HORSE_RACING("경륜"),
    HORSE_RACING_TRACK("경마"),
    GOLF("골프"),
    OTHER_LAND_LEISURE("기타육상레저스포츠"),
    BUNGEE_JUMP("번지점프"),
    SHOOTING_RANGE("사격장"),
    SURVIVAL_GAME("서바이벌게임"),
    HUNTING_GROUND("수렵장"),
    SKATING("스케이트"),
    SKI_SNOWBOARD("스키/스노보드"),
    HORSEBACK_RIDING("승마"),
    SLEDDING("썰매장"),
    ROCK_CLIMBING("암벽등반"),
    OFF_ROAD("오프로드"),
    INLINE_SKATING("인라인(실내 인라인 포함)"),
    BICYCLE_HIKING("자전거하이킹"),
    GO_KART("카트"),

    // 레저스포츠 - 수상레저스포츠 LS02
    OTHER_WATER_LEISURE("기타수상레저스포츠"),
    RAFTING("래프팅"),
    FRESHWATER_FISHING("민물낚시"),
    SEA_FISHING("바다낚시"),
    WATER_MOTORCYCLE("수상오토바이"),
    WATER_BICYCLE("수상자전거"),
    SWIMMING("수영"),
    SNORKELING_SCUBA("스노쿨링/스킨스쿠버다이빙"),
    YACHT("요트"),
    WATER_SLED("워터슬레드"),
    WINDSURFING_JETSKI("윈드서핑/제트스키"),
    ROWING("조정"),
    KAYAK_CANOE("카약/카누"),
    PARASAIL("패러세일"),

    // 레저스포츠 - 항공레저스포츠 LS03
    OTHER_AERIAL_LEISURE("기타항공레저스포츠"),
    DRONE("무인비행장치(드론)"),
    SKYDIVING("스카이다이빙"),
    HOT_AIR_BALLOON("열기구"),
    ULTRALIGHT_AIRCRAFT("초경량비행"),
    HANG_GLIDING_PARAGLIDING("헹글라이딩/패러글라이딩"),

    // 레저스포츠 - 복합레저스포츠 LS04
    COMPLEX_LEISURE_SPORTS_DETAIL("복합레저스포츠"),

    // 문화관광 - 랜드마크관광 VE01
    BUILDING("건물"),
    OTHER_ARCHITECTURE("기타 건축/조형물"),
    BRIDGE("다리 / 대교"), //공백 주의
    DAM("댐"),
    STATUE("동상"),
    LIGHTHOUSE("등대"),
    FOUNTAIN("분수"),
    TOWER_OBSERVATORY("타워 / 전망대"), //공백 주의
    TUNNEL("터널"),

    // 문화관광 - 테마공원 VE02
    ZOO("동물원"),
    AQUARIUM("수족관 / 아쿠라리움"), //공백 주의
    WATER_PARK("워터파크"),
    PLANETARIUM("천문대"),
    THEME_PARK_DETAIL("테마파크"),

    // 문화관광 - 도시공원 VE03
    NEIGHBORHOOD_PARK("근린공원"),
    SMALL_PARK("소공원"),
    CIVIC_PARK("시민공원"),
    CHILDREN_PARK("어린이공원"),
    THEME_PARK_CITY("주제공원"),

    // 문화관광 - 도시.지역문화관광 VE04
    ALLEY_CULTURAL_STREET("골목길, 문화거리"),
    TRAIL("둘레길"),
    VILLAGE_TOURISM("마을관광지"),

    // 문화관광 - 복합관광시설 VE05
    TOURISM_COMPLEX("관광단지"),
    RESORT("리조트"),

    // 문화관광 - 공연시설 VE06
    PERFORMANCE_HALL("공연장"),
    CINEMA("영화관"),

    // 문화관광 - 전시시설 VE07
    SCIENCE_MUSEUM("과학관"),
    MEMORIAL_HALL("기념관"),
    ART_GALLERY("미술관/화랑"),
    MUSEUM("박물관"),
    EXHIBITION_HALL("전시관"),
    CONVENTION_CENTER("컨벤션센터"),

    // 문화관광 - 교육시설 VE09
    LIBRARY("도서관"),
    CULTURAL_CENTER("문화전수시설"),
    LANGUAGE_SCHOOL("어학당"),
    FOREIGN_CULTURAL_CENTER("외국문화원"),
    SCHOOL("학교"),
    KOREAN_CULTURAL_CENTER("한국문화원"),

    // 문화관광 - 레저스포츠시설 VE10
    SPORTS_STADIUM("스포츠경기장"),
    SPORTS_CENTER_TRAINING_FACILITY("스포츠센터, 수련시설"),


    // 문화관광 - 기타문화관광지 VE12
    OTHER_CULTURAL_FACILITY("기타문화시설"),
    BOOKSTORE("서점"),
    CASINO("카지노"),

    // 쇼핑 - 백화점 SH01
    DEPARTMENT_STORE_DETAIL("백화점"),

    // 쇼핑 - 쇼핑몰 SH02
    COMPLEX_SHOPPING_MALL("복합쇼핑몰"),
    OUTLET("아웃렛"),

    // 쇼핑 - 대형마트 SH03
    LARGE_MART_DETAIL("대형마트"),

    // 쇼핑 - 면세점 SH04
    AIRPORT_DUTY_FREE("공항면세점"),
    POST_DUTY_FREE("사후면세점"),
    DOWNTOWN_DUTY_FREE("시내면세점"),

    // 쇼핑 - 전문매장/상가 SH05
    CRAFT_SHOP("공방/공예품점"),
    SOUVENIR_SHOP("관광기념품/특산물판매점"),
    SPECIALIZED_MALL("전문상가"),

    // 쇼핑 - 시장 SH06
    TEMPORARY_MARKET("비상설시장"),
    PERMANENT_MARKET("상설시장"),

    // 쇼핑 - 기타쇼핑시설 SH07
    OTHER_SHOPPING_FACILITY_DETAIL("기타쇼핑시설"),

    // 숙박 - 호텔 AC01
    HOTEL_DETAIL("호텔"),

    // 숙박 - 콘도미니엄 AC02
    RESIDENCE("레지던스"),
    CONDO("콘도"),

    // 숙박 - 펜션/민박 AC03
    RURAL_HOMESTAY("농어촌민박"),
    PENSION("펜션"),
    HANOK_STAY("한옥스테이"),
    HOME_STAY("홈스테이"),

    // 숙박 - 모텔 AC04
    MOTEL_DETAIL("모텔"),

    // 숙박 - 캠핑 AC05
    GLAMPING("글램핑장"),
    AUTO_CAMPING("오토캠핑장"),
    GENERAL_CAMPING("일반야영장"),
    CARAVAN("카라반"),

    // 숙박 - 호스텔 AC06
    GUEST_HOUSE("게스트하우스"),
    YOUTH_HOSTEL("유스호스텔"),

    // 역사관광 - 역사유적지 HS01
    PALACE("고궁"),
    ANCIENT_ROYAL_TOMB("고분, 능"),
    TRADITIONAL_HOUSE("고택"),
    MODERN_ARCHITECTURE("근대건축물"),
    OTHER_HISTORIC_SITE("기타역사유적지"),
    GATE("문"),
    FOLK_VILLAGE("민속마을"),
    SHRINE("사당"),
    HISTORIC_SITE_DETAIL("사적지"),
    BIRTHPLACE("생가"),
    PREHISTORIC_SITE("선사유적지"),
    FORTRESS("성ㆍ산성ㆍ성곽"),

    // 역사관광 - 역사유물 HS02
    OTHER_HISTORIC_RELIC("기타역사유물"),
    BUDDHA_STATUE("불상"),
    PREHISTORIC_RELIC("선사유물"),
    PAGODA_MONUMENT("탑ㆍ비석ㆍ기념탑"),

    // 역사관광 - 종교성지 HS03
    CHRISTIANITY("기독교"),
    OTHER_RELIGIOUS_SITE("기타 종교성지"),
    BUDDHISM("불교"),
    ISLAM("이슬람"),

    // 역사관광 - 안보관광지 HS04
    OTHER_SECURITY_TOURISM("기타안보관광지"),
    NORTH_KOREA_TOURISM("북한관광지"),
    SECURITY_TOURISM_FACILITY("안보관광시설"),
    SECURITY_HISTORIC_SITE("안보유적지"),

    // 음식 - 한식 FD01
    TOURIST_RESTAURANT("관광식당"),
    EXEMPLARY_RESTAURANT("모범음식점"),

    // 음식 - 외국식 FD02
    OTHER_FOREIGN_FOOD("기타외국식"),
    WESTERN_FOOD("서양식"),
    JAPANESE_FOOD("일식"),
    CHINESE_FOOD("중식"),
    FUSION_FOOD("퓨전음식"),

    // 음식 - 간이음식 FD03
    OTHER_SIMPLE_FOOD("기타간이음식"),
    KIMBAP_SNACK("김밥 분식"),
    MOBILE_FOOD("이동음식"),
    BAKERY("제과"),
    CHICKEN("치킨"),
    PIZZA_HAMBURGER_SANDWICH("피자, 햄버거, 샌드위치 및 유사음식"),

    // 음식 - 주점 FD04
    OTHER_PUB("기타주점"),
    BAR_PUB("바/펍"),
    DRAFT_BEER("생맥주전문점"),
    TRADITIONAL_LIQUOR("전통주/민속주점"),
    CLUB("클럽"),

    // 음식 - 카페/찻집 FD05
    OTHER_BEVERAGE("기타음료점"),
    TEA_HOUSE_DETAIL("찻집"),
    CAFE_DETAIL("카페"),

    // 자연관광 - 자연경관(산)NA01
    VALLEY("계곡"),
    MOUNTAIN("산, 고개, 오름, 봉우리"),
    FOREST("숲"),
    MINERAL_SPRING("약수터"),
    WATERFALL("폭포"),

    // 자연관광 - 자연경관(하천‧해양)
    RIVER("강"),
    ISLAND("섬"),
    POND_SWAMP("연못·늪"),
    SALT_FIELD("염전"),
    RESERVOIR("저수지"),
    PORT_HARBOR("항구/포구"),
    BEACH("해변. 해수욕장"),
    COASTAL_SCENERY("해안절경"),
    LAKE("호수"),

    // 자연관광 - 자연생태
    ROCK_FORMATION("기암괴석"),
    OTHER_NATURAL_ECOLOGY("기타자연생태"),
    CAVE("동굴"),
    ECOLOGICAL_WETLAND("생태습지"),
    RARE_FLORA_FAUNA("희귀동.식물"),

    // 자연관광 - 자연공원
    NATIONAL_PARK("국립공원"),
    COUNTY_PARK("군립공원"),
    PROVINCIAL_PARK("도립공원"),
    ECO_TOURISM_SITE("생태관광지"),
    ARBORETUM_GARDEN("수목원ㆍ정원"),
    NATURAL_RECREATION_FOREST("자연휴양림"),
    GEOPARK("지질공원"),

    // 자연관광 - 기타자연관광
    OTHER_NATURE_TOURISM("기타자연관광"),

    // 체험관광 - 전통체험 EX01
    TRADITIONAL_CULTURE_EXPERIENCE("전통문화체험"),

    // 체험관광 - 공예체험 EX02
    LEATHER_CRAFT("가죽공예체험"),
    METAL_CRAFT("금속공예체험"),
    OTHER_CRAFT_EXPERIENCE("기타공예체험"),
    GLASS_CRAFT("유리공예체험"),

    // 체험관광 - 농.산.어촌 체험 EX03
    EXPERIENCE_FARM("체험농장"),
    EXPERIENCE_VILLAGE("체험마을"),
    EXPERIENCE_RANCH("체험목장"),
    EXPERIENCE_FISHERY("체험어장"),

    // 체험관광 - 산사체험 EX04
    TEMPLE_CULTURE_EXPERIENCE("사찰문화체험"),
    TEMPLE_STAY("템플스테이"),

    // 체험관광 - 웰니스관광 EX05
    OTHER_WELLNESS("기타웰니스"),
    BEAUTY_SPA("뷰티스파"),
    HOT_SPRING_SAUNA_SPA("온천 / 사우나 / 스파"),
    NATURAL_HEALING("자연치유"),
    JJIMJILBANG("찜질방"),
    TRADITIONAL_MEDICINE("한방체험"),
    HEALING_MEDITATION("힐링명상"),

    // 체험관광 - 산업관광 EX06
    GAME_IT_INDUSTRY("게임 등 첨단IT산업"),
    MODERN_INDUSTRIAL_HERITAGE("근대산업유산"),
    OTHER_INDUSTRIAL_TOURISM("기타산업관광지"),
    ROBOT_AEROSPACE("로봇/항공우주산업"),
    CULTURAL_CONTENT("문화콘텐츠산업"),
    AUTOMOTIVE_SHIPBUILDING("자동차/조선/철강 등"),
    LONG_ESTABLISHED_BUSINESS("장수기업/산업테마거리"),
    TRADITIONAL_LOCAL_INDUSTRY("전통/향토산업"),
    ECO_RENEWABLE_ENERGY("친환경/신재생에너지"),
    COSMETICS_LIQUOR_FOOD("화장품/주류/먹거리"),

    // 체험관광 - 기타체험 EX07
    OTHER_EXPERIENCE_TOURISM("기타체험관광"),
    CRUISE_SUBMARINE("유람선/잠수함관광"),


    // 축제/공연/행사 - 축제 EV01
    OTHER_FESTIVAL("기타축제"),
    CULTURAL_TOURISM_FESTIVAL("문화관광축제"),
    CULTURAL_ART_FESTIVAL("문화예술축제"),
    ECOLOGICAL_NATURE_FESTIVAL("생태자연축제"),
    TRADITIONAL_HISTORY_FESTIVAL("전통역사축제"),
    LOCAL_SPECIALTY_FESTIVAL("지역특산물축제"),

    // 축제/공연/행사 - 공연 EV02
    OTHER_PERFORMANCE("기타공연"),
    NON_VERBAL("넌버벌"),
    POPULAR_CONCERT("대중콘서트"),
    DANCE("무용"),
    MUSICAL("뮤지컬"),
    THEATER("연극"),
    MOVIE("영화"),
    OPERA("오페라"),
    TRADITIONAL_PERFORMANCE("전통공연"),
    CLASSICAL_CONCERT("클래식음악회"),

    // 축제/공연/행사 - 행사 EV03
    OTHER_EVENT("기타행사"),
    EXPO("박람회"),
    SPORTS_EVENT("스포츠경기"),
    EXHIBITION("전시회");

    private final String koreanName;

    DetailCategory(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }

    /**
     * 한국어 이름으로 DetailCategory 찾기
     */
    public static DetailCategory findByKoreanName(String koreanName) {
        for (DetailCategory category : values()) {
            if (category.getKoreanName().equals(koreanName)) {
                return category;
            }
        }
        return null;
    }
}