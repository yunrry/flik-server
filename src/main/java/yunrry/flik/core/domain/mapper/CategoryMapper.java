package yunrry.flik.core.domain.mapper;

import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.model.DetailCategory;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.SubCategory;

import java.util.List;
import java.util.Map;

@Service
public class CategoryMapper {

    private static final Map<MainCategory, List<SubCategory>> CATEGORY_MAPPING = Map.of(
            MainCategory.NATURE, List.of(
                    SubCategory.MOUNTAIN_SCENERY,
                    SubCategory.WATER_SCENERY,
                    SubCategory.NATURAL_ECOLOGY,
                    SubCategory.NATURAL_PARK,
                    SubCategory.TEMPLE_EXPERIENCE,
                    SubCategory.OTHER_NATURE
            ),

            MainCategory.INDOOR, List.of(
                    SubCategory.EXHIBITION_FACILITY,
                    SubCategory.PERFORMANCE_FACILITY,
                    SubCategory.EDUCATION_FACILITY,
                    SubCategory.SHOPPING_MALL,
                    SubCategory.DEPARTMENT_STORE,
                    SubCategory.LARGE_MART
            ),

            MainCategory.HISTORY_CULTURE, List.of(
                    SubCategory.HISTORIC_SITE,
                    SubCategory.HISTORIC_RELIC,
                    SubCategory.RELIGIOUS_SITE,
                    SubCategory.SECURITY_TOURISM,
                    SubCategory.OTHER_CULTURAL_SITE,
                    SubCategory.LANDMARK_TOURISM,
                    SubCategory.CITY_PARK,
                    SubCategory.CITY_REGIONAL_CULTURE,
                    SubCategory.INDUSTRIAL_TOURISM
            ),

            MainCategory.CAFE, List.of(
                    SubCategory.CAFE_TEA_HOUSE
            ),

            MainCategory.ACTIVITY, List.of(
                    SubCategory.LAND_LEISURE_SPORTS,
                    SubCategory.WATER_LEISURE_SPORTS,
                    SubCategory.AVIATION_LEISURE_SPORTS,
                    SubCategory.TRADITIONAL_EXPERIENCE,
                    SubCategory.CRAFT_EXPERIENCE,
                    SubCategory.FARM_VILLAGE_EXPERIENCE,
                    SubCategory.WELLNESS_TOURISM,
                    SubCategory.COMPLEX_LEISURE_SPORTS,
                    SubCategory.LEISURE_SPORTS_FACILITY,
                    SubCategory.OTHER_EXPERIENCE
            ),

            MainCategory.FESTIVAL, List.of(
                    SubCategory.FESTIVAL,
                    SubCategory.PERFORMANCE,
                    SubCategory.EVENT
            ),

            MainCategory.MARKET, List.of(
                    SubCategory.MARKET,
                    SubCategory.SPECIALTY_STORE,
                    SubCategory.DUTY_FREE_SHOP,
                    SubCategory.OTHER_SHOPPING_FACILITY
            ),

            MainCategory.THEMEPARK, List.of(
                    SubCategory.THEME_PARK,
                    SubCategory.COMPLEX_TOURISM_FACILITY
            ),

            MainCategory.RESTAURANT, List.of(
                    SubCategory.KOREAN_FOOD,
                    SubCategory.FOREIGN_FOOD,
                    SubCategory.SIMPLE_FOOD,
                    SubCategory.PUB_BAR
            ),

            MainCategory.ACCOMMODATION, List.of(
                    SubCategory.HOTEL,
                    SubCategory.CONDOMINIUM,
                    SubCategory.PENSION_HOMESTAY,
                    SubCategory.MOTEL,
                    SubCategory.CAMPING,
                    SubCategory.HOSTEL
            )
    );


    private static final Map<SubCategory, List<DetailCategory>> SUB_DETAIL_MAPPING = Map.ofEntries(
            // 레저스포츠 - 육상레저스포츠
            Map.entry(SubCategory.LAND_LEISURE_SPORTS, List.of(
                    DetailCategory.ATV,
                    DetailCategory.MTB,
                    DetailCategory.HORSE_RACING,
                    DetailCategory.HORSE_RACING_TRACK,
                    DetailCategory.GOLF,
                    DetailCategory.OTHER_LAND_LEISURE,
                    DetailCategory.BUNGEE_JUMP,
                    DetailCategory.SHOOTING_RANGE,
                    DetailCategory.SURVIVAL_GAME,
                    DetailCategory.HUNTING_GROUND,
                    DetailCategory.SKATING,
                    DetailCategory.SKI_SNOWBOARD,
                    DetailCategory.HORSEBACK_RIDING,
                    DetailCategory.SLEDDING,
                    DetailCategory.ROCK_CLIMBING,
                    DetailCategory.OFF_ROAD,
                    DetailCategory.INLINE_SKATING,
                    DetailCategory.BICYCLE_HIKING,
                    DetailCategory.GO_KART
            )),

            // 레저스포츠 - 수상레저스포츠
            Map.entry(SubCategory.WATER_LEISURE_SPORTS, List.of(
                    DetailCategory.OTHER_WATER_LEISURE,
                    DetailCategory.RAFTING,
                    DetailCategory.FRESHWATER_FISHING,
                    DetailCategory.SEA_FISHING,
                    DetailCategory.WATER_MOTORCYCLE,
                    DetailCategory.WATER_BICYCLE,
                    DetailCategory.SWIMMING,
                    DetailCategory.SNORKELING_SCUBA,
                    DetailCategory.YACHT,
                    DetailCategory.WATER_SLED,
                    DetailCategory.WINDSURFING_JETSKI,
                    DetailCategory.ROWING,
                    DetailCategory.KAYAK_CANOE,
                    DetailCategory.PARASAIL
            )),

            // 레저스포츠 - 항공레저스포츠
            Map.entry(SubCategory.AVIATION_LEISURE_SPORTS, List.of(
                    DetailCategory.OTHER_AERIAL_LEISURE,
                    DetailCategory.DRONE,
                    DetailCategory.SKYDIVING,
                    DetailCategory.HOT_AIR_BALLOON,
                    DetailCategory.ULTRALIGHT_AIRCRAFT,
                    DetailCategory.HANG_GLIDING_PARAGLIDING
            )),

            // 문화관광 - 랜드마크관광
            Map.entry(SubCategory.LANDMARK_TOURISM, List.of(
                    DetailCategory.BUILDING,
                    DetailCategory.OTHER_ARCHITECTURE,
                    DetailCategory.BRIDGE,
                    DetailCategory.DAM,
                    DetailCategory.STATUE,
                    DetailCategory.LIGHTHOUSE,
                    DetailCategory.FOUNTAIN,
                    DetailCategory.TOWER_OBSERVATORY,
                    DetailCategory.TUNNEL
            )),

            // 문화관광 - 테마공원
            Map.entry(SubCategory.THEME_PARK, List.of(
                    DetailCategory.ZOO,
                    DetailCategory.AQUARIUM,
                    DetailCategory.WATER_PARK,
                    DetailCategory.PLANETARIUM,
                    DetailCategory.THEME_PARK_DETAIL
            )),

            // 문화관광 - 도시공원
            Map.entry(SubCategory.CITY_PARK, List.of(
                    DetailCategory.NEIGHBORHOOD_PARK,
                    DetailCategory.SMALL_PARK,
                    DetailCategory.CIVIC_PARK,
                    DetailCategory.CHILDREN_PARK,
                    DetailCategory.THEME_PARK_CITY
            )),

            // 쇼핑 - 백화점
            Map.entry(SubCategory.DEPARTMENT_STORE, List.of(
                    DetailCategory.DEPARTMENT_STORE_DETAIL
            )),

            // 쇼핑 - 쇼핑몰
            Map.entry(SubCategory.SHOPPING_MALL, List.of(
                    DetailCategory.COMPLEX_SHOPPING_MALL,
                    DetailCategory.OUTLET
            )),

            // 쇼핑 - 대형마트
            Map.entry(SubCategory.LARGE_MART, List.of(
                    DetailCategory.LARGE_MART_DETAIL
            )),

            // 숙박 - 호텔
            Map.entry(SubCategory.HOTEL, List.of(
                    DetailCategory.HOTEL_DETAIL
            )),

            // 숙박 - 펜션/민박
            Map.entry(SubCategory.PENSION_HOMESTAY, List.of(
                    DetailCategory.RURAL_HOMESTAY,
                    DetailCategory.PENSION,
                    DetailCategory.HANOK_STAY,
                    DetailCategory.HOME_STAY
            )),

            // 숙박 - 캠핑
            Map.entry(SubCategory.CAMPING, List.of(
                    DetailCategory.GLAMPING,
                    DetailCategory.AUTO_CAMPING,
                    DetailCategory.GENERAL_CAMPING,
                    DetailCategory.CARAVAN
            )),

            // 음식 - 한식
            Map.entry(SubCategory.KOREAN_FOOD, List.of(
                    DetailCategory.TOURIST_RESTAURANT,
                    DetailCategory.EXEMPLARY_RESTAURANT
            )),

            // 음식 - 카페/찻집
            Map.entry(SubCategory.CAFE_TEA_HOUSE, List.of(
                    DetailCategory.OTHER_BEVERAGE,
                    DetailCategory.TEA_HOUSE_DETAIL,
                    DetailCategory.CAFE_DETAIL
            )),

            // 자연관광 - 자연경관(산)
            Map.entry(SubCategory.MOUNTAIN_SCENERY, List.of(
                    DetailCategory.VALLEY,
                    DetailCategory.MOUNTAIN,
                    DetailCategory.FOREST,
                    DetailCategory.MINERAL_SPRING,
                    DetailCategory.WATERFALL
            )),

            // 자연관광 - 자연경관(하천‧해양)
            Map.entry(SubCategory.WATER_SCENERY, List.of(
                    DetailCategory.RIVER,
                    DetailCategory.ISLAND,
                    DetailCategory.POND_SWAMP,
                    DetailCategory.SALT_FIELD,
                    DetailCategory.RESERVOIR,
                    DetailCategory.PORT_HARBOR,
                    DetailCategory.BEACH,
                    DetailCategory.COASTAL_SCENERY,
                    DetailCategory.LAKE
            )),

            // 자연관광 - 자연생태
            Map.entry(SubCategory.NATURAL_ECOLOGY, List.of(
                    DetailCategory.ROCK_FORMATION,
                    DetailCategory.OTHER_NATURAL_ECOLOGY,
                    DetailCategory.CAVE,
                    DetailCategory.ECOLOGICAL_WETLAND,
                    DetailCategory.RARE_FLORA_FAUNA
            )),

            // 자연관광 - 자연공원
            Map.entry(SubCategory.NATURAL_PARK, List.of(
                    DetailCategory.NATIONAL_PARK,
                    DetailCategory.COUNTY_PARK,
                    DetailCategory.PROVINCIAL_PARK,
                    DetailCategory.ECO_TOURISM_SITE,
                    DetailCategory.ARBORETUM_GARDEN,
                    DetailCategory.NATURAL_RECREATION_FOREST,
                    DetailCategory.GEOPARK
            )),

            // 체험관광 - 전통체험
            Map.entry(SubCategory.TRADITIONAL_EXPERIENCE, List.of(
                    DetailCategory.TRADITIONAL_CULTURE_EXPERIENCE
            )),

            // 체험관광 - 웰니스관광
            Map.entry(SubCategory.WELLNESS_TOURISM, List.of(
                    DetailCategory.OTHER_WELLNESS,
                    DetailCategory.BEAUTY_SPA,
                    DetailCategory.HOT_SPRING_SAUNA_SPA,
                    DetailCategory.NATURAL_HEALING,
                    DetailCategory.JJIMJILBANG,
                    DetailCategory.TRADITIONAL_MEDICINE,
                    DetailCategory.HEALING_MEDITATION
            )),

            // 축제/공연/행사 - 축제
            Map.entry(SubCategory.FESTIVAL, List.of(
                    DetailCategory.OTHER_FESTIVAL,
                    DetailCategory.CULTURAL_TOURISM_FESTIVAL,
                    DetailCategory.CULTURAL_ART_FESTIVAL,
                    DetailCategory.ECOLOGICAL_NATURE_FESTIVAL,
                    DetailCategory.TRADITIONAL_HISTORY_FESTIVAL,
                    DetailCategory.LOCAL_SPECIALTY_FESTIVAL
            )),

            // 레저스포츠 - 복합레저스포츠
            Map.entry(SubCategory.COMPLEX_LEISURE_SPORTS, List.of(
                    DetailCategory.COMPLEX_LEISURE_SPORTS_DETAIL
            )),

            // 전시시설
            Map.entry(SubCategory.EXHIBITION_FACILITY, List.of(
                    DetailCategory.SCIENCE_MUSEUM,
                    DetailCategory.MEMORIAL_HALL,
                    DetailCategory.ART_GALLERY,
                    DetailCategory.MUSEUM,
                    DetailCategory.EXHIBITION_HALL,
                    DetailCategory.CONVENTION_CENTER
            )),

            // 공연시설
            Map.entry(SubCategory.PERFORMANCE_FACILITY, List.of(
                    DetailCategory.PERFORMANCE_HALL,
                    DetailCategory.CINEMA
            )),

            // 교육시설
            Map.entry(SubCategory.EDUCATION_FACILITY, List.of(
                    DetailCategory.LIBRARY,
                    DetailCategory.CULTURAL_CENTER,
                    DetailCategory.LANGUAGE_SCHOOL,
                    DetailCategory.FOREIGN_CULTURAL_CENTER,
                    DetailCategory.SCHOOL,
                    DetailCategory.KOREAN_CULTURAL_CENTER
            )),

            // 역사유적지
            Map.entry(SubCategory.HISTORIC_SITE, List.of(
                    DetailCategory.PALACE,
                    DetailCategory.ANCIENT_ROYAL_TOMB,
                    DetailCategory.TRADITIONAL_HOUSE,
                    DetailCategory.MODERN_ARCHITECTURE,
                    DetailCategory.OTHER_HISTORIC_SITE,
                    DetailCategory.GATE,
                    DetailCategory.FOLK_VILLAGE,
                    DetailCategory.SHRINE,
                    DetailCategory.HISTORIC_SITE_DETAIL,
                    DetailCategory.BIRTHPLACE,
                    DetailCategory.PREHISTORIC_SITE,
                    DetailCategory.FORTRESS
            )),

            // 역사유물
            Map.entry(SubCategory.HISTORIC_RELIC, List.of(
                    DetailCategory.OTHER_HISTORIC_RELIC,
                    DetailCategory.BUDDHA_STATUE,
                    DetailCategory.PREHISTORIC_RELIC,
                    DetailCategory.PAGODA_MONUMENT
            )),

            // 종교성지
            Map.entry(SubCategory.RELIGIOUS_SITE, List.of(
                    DetailCategory.CHRISTIANITY,
                    DetailCategory.OTHER_RELIGIOUS_SITE,
                    DetailCategory.BUDDHISM,
                    DetailCategory.ISLAM
            )),

            // 안보관광지
            Map.entry(SubCategory.SECURITY_TOURISM, List.of(
                    DetailCategory.OTHER_SECURITY_TOURISM,
                    DetailCategory.NORTH_KOREA_TOURISM,
                    DetailCategory.SECURITY_TOURISM_FACILITY,
                    DetailCategory.SECURITY_HISTORIC_SITE
            )),

            // 기타문화관광지
            Map.entry(SubCategory.OTHER_CULTURAL_SITE, List.of(
                    DetailCategory.OTHER_CULTURAL_FACILITY,
                    DetailCategory.BOOKSTORE,
                    DetailCategory.CASINO
            )),

            // 도시.지역문화관광
            Map.entry(SubCategory.CITY_REGIONAL_CULTURE, List.of(
                    DetailCategory.ALLEY_CULTURAL_STREET,
                    DetailCategory.TRAIL,
                    DetailCategory.VILLAGE_TOURISM
            )),

            // 산업관광
            Map.entry(SubCategory.INDUSTRIAL_TOURISM, List.of(
                    DetailCategory.GAME_IT_INDUSTRY,
                    DetailCategory.MODERN_INDUSTRIAL_HERITAGE,
                    DetailCategory.OTHER_INDUSTRIAL_TOURISM,
                    DetailCategory.ROBOT_AEROSPACE,
                    DetailCategory.CULTURAL_CONTENT,
                    DetailCategory.AUTOMOTIVE_SHIPBUILDING,
                    DetailCategory.LONG_ESTABLISHED_BUSINESS,
                    DetailCategory.TRADITIONAL_LOCAL_INDUSTRY,
                    DetailCategory.ECO_RENEWABLE_ENERGY,
                    DetailCategory.COSMETICS_LIQUOR_FOOD
            )),

            // 공예체험
            Map.entry(SubCategory.CRAFT_EXPERIENCE, List.of(
                    DetailCategory.LEATHER_CRAFT,
                    DetailCategory.METAL_CRAFT,
                    DetailCategory.OTHER_CRAFT_EXPERIENCE,
                    DetailCategory.GLASS_CRAFT
            )),

            // 농.산.어촌체험
            Map.entry(SubCategory.FARM_VILLAGE_EXPERIENCE, List.of(
                    DetailCategory.EXPERIENCE_FARM,
                    DetailCategory.EXPERIENCE_VILLAGE,
                    DetailCategory.EXPERIENCE_RANCH,
                    DetailCategory.EXPERIENCE_FISHERY
            )),

            // 산사체험
            Map.entry(SubCategory.TEMPLE_EXPERIENCE, List.of(
                    DetailCategory.TEMPLE_CULTURE_EXPERIENCE,
                    DetailCategory.TEMPLE_STAY
            )),

            // 레저스포츠시설
            Map.entry(SubCategory.LEISURE_SPORTS_FACILITY, List.of(
                    DetailCategory.SPORTS_STADIUM,
                    DetailCategory.SPORTS_CENTER_TRAINING_FACILITY
            )),

            // 기타체험
            Map.entry(SubCategory.OTHER_EXPERIENCE, List.of(
                    DetailCategory.OTHER_EXPERIENCE_TOURISM,
                    DetailCategory.CRUISE_SUBMARINE
            )),

            // 공연
            Map.entry(SubCategory.PERFORMANCE, List.of(
                    DetailCategory.OTHER_PERFORMANCE,
                    DetailCategory.NON_VERBAL,
                    DetailCategory.POPULAR_CONCERT,
                    DetailCategory.DANCE,
                    DetailCategory.MUSICAL,
                    DetailCategory.THEATER,
                    DetailCategory.MOVIE,
                    DetailCategory.OPERA,
                    DetailCategory.TRADITIONAL_PERFORMANCE,
                    DetailCategory.CLASSICAL_CONCERT
            )),

            // 행사
            Map.entry(SubCategory.EVENT, List.of(
                    DetailCategory.OTHER_EVENT,
                    DetailCategory.EXPO,
                    DetailCategory.SPORTS_EVENT,
                    DetailCategory.EXHIBITION
            )),

            // 시장
            Map.entry(SubCategory.MARKET, List.of(
                    DetailCategory.TEMPORARY_MARKET,
                    DetailCategory.PERMANENT_MARKET
            )),

            // 전문매장/상가
            Map.entry(SubCategory.SPECIALTY_STORE, List.of(
                    DetailCategory.CRAFT_SHOP,
                    DetailCategory.SOUVENIR_SHOP,
                    DetailCategory.SPECIALIZED_MALL
            )),

            // 면세점
            Map.entry(SubCategory.DUTY_FREE_SHOP, List.of(
                    DetailCategory.AIRPORT_DUTY_FREE,
                    DetailCategory.POST_DUTY_FREE,
                    DetailCategory.DOWNTOWN_DUTY_FREE
            )),

            // 기타쇼핑시설
            Map.entry(SubCategory.OTHER_SHOPPING_FACILITY, List.of(
                    DetailCategory.OTHER_SHOPPING_FACILITY_DETAIL
            )),

            // 복합관광시설
            Map.entry(SubCategory.COMPLEX_TOURISM_FACILITY, List.of(
                    DetailCategory.TOURISM_COMPLEX,
                    DetailCategory.RESORT
            )),

            // 외국식
            Map.entry(SubCategory.FOREIGN_FOOD, List.of(
                    DetailCategory.OTHER_FOREIGN_FOOD,
                    DetailCategory.WESTERN_FOOD,
                    DetailCategory.JAPANESE_FOOD,
                    DetailCategory.CHINESE_FOOD,
                    DetailCategory.FUSION_FOOD
            )),

            // 간이음식
            Map.entry(SubCategory.SIMPLE_FOOD, List.of(
                    DetailCategory.OTHER_SIMPLE_FOOD,
                    DetailCategory.KIMBAP_SNACK,
                    DetailCategory.MOBILE_FOOD,
                    DetailCategory.BAKERY,
                    DetailCategory.CHICKEN,
                    DetailCategory.PIZZA_HAMBURGER_SANDWICH
            )),

            // 주점
            Map.entry(SubCategory.PUB_BAR, List.of(
                    DetailCategory.OTHER_PUB,
                    DetailCategory.BAR_PUB,
                    DetailCategory.DRAFT_BEER,
                    DetailCategory.TRADITIONAL_LIQUOR,
                    DetailCategory.CLUB
            )),

            // 콘도미니엄
            Map.entry(SubCategory.CONDOMINIUM, List.of(
                    DetailCategory.RESIDENCE,
                    DetailCategory.CONDO
            )),

            // 모텔
            Map.entry(SubCategory.MOTEL, List.of(
                    DetailCategory.MOTEL_DETAIL
            )),

            // 호스텔
            Map.entry(SubCategory.HOSTEL, List.of(
                    DetailCategory.GUEST_HOUSE,
                    DetailCategory.YOUTH_HOSTEL
            )),

            // 기타자연관광
            Map.entry(SubCategory.OTHER_NATURE, List.of(
                    DetailCategory.OTHER_NATURE_TOURISM
            ))
    );


    /**
     * SubCategory에 해당하는 DetailCategory 목록 조회
     */
    public List<DetailCategory> getDetailCategories(SubCategory subCategory) {
        return SUB_DETAIL_MAPPING.getOrDefault(subCategory, List.of());
    }

    /**
     * DetailCategory가 속한 SubCategory 찾기
     */
    public static SubCategory getSubCategory(DetailCategory detailCategory) {
        return SUB_DETAIL_MAPPING.entrySet().stream()
                .filter(entry -> entry.getValue().contains(detailCategory))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }


    public static MainCategory getMainCategory(SubCategory subCategory) {
        return CATEGORY_MAPPING.entrySet().stream()
                .filter(entry -> entry.getValue().contains(subCategory))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * 지원하는 모든 SubCategory 조회
     */
    public List<SubCategory> getSupportedSubCategories() {
        return SUB_DETAIL_MAPPING.keySet().stream().toList();
    }

    /**
     * 한국어 이름으로 DetailCategory 찾기
     */
    public DetailCategory findDetailCategoryByKoreanName(String koreanName) {
        return DetailCategory.findByKoreanName(koreanName);
    }

    /**
     * 카테고리에 해당하는 SubCategory 목록 반환
     */
    public List<SubCategory> getSubCategories(String category) {
        return CATEGORY_MAPPING.getOrDefault(category, List.of());
    }

    /**
     * 카테고리에 해당하는 한국어 중분류명 목록 반환 (DB 검색용)
     */
    public List<String> getSubCategoryNames(String category) {
        // String을 MainCategory enum으로 변환
        MainCategory mainCategory = MainCategory.findByCode(category);

        if (mainCategory == null) {
            System.out.println("Invalid category code: " + category);
            return List.of();
        }

        List<String> result = CATEGORY_MAPPING.getOrDefault(mainCategory, List.of())
                .stream()
                .map(SubCategory::getKoreanName)
                .toList();

        System.out.println("Category: " + category + " -> MainCategory: " + mainCategory + " -> SubCategories: " + result);
        return result;
    }

    /**
     * 카테고리 지원 여부 확인
     */
    public boolean isValidCategory(String category) {
        MainCategory mainCategory = MainCategory.findByCode(category);
        return mainCategory != null && CATEGORY_MAPPING.containsKey(mainCategory);
    }

    /**
     * MainCategory enum으로 직접 조회하는 메서드 추가
     */
    public List<String> getSubCategoryNames(MainCategory mainCategory) {
        return CATEGORY_MAPPING.getOrDefault(mainCategory, List.of())
                .stream()
                .map(SubCategory::getKoreanName)
                .toList();
    }



}