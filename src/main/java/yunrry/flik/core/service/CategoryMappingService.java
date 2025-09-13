package yunrry.flik.core.service;

import org.springframework.stereotype.Service;
import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.SubCategory;

import java.util.List;
import java.util.Map;

@Service
public class CategoryMappingService {

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
        return CATEGORY_MAPPING.getOrDefault(category, List.of())
                .stream()
                .map(SubCategory::getKoreanName)
                .toList();
    }

    /**
     * 지원하는 카테고리 목록 반환
     */
    public List<MainCategory> getSupportedCategories() {
        return List.copyOf(CATEGORY_MAPPING.keySet());
    }

    /**
     * 카테고리 지원 여부 확인
     */
    public boolean isValidCategory(String category) {
        return CATEGORY_MAPPING.containsKey(category);
    }
}