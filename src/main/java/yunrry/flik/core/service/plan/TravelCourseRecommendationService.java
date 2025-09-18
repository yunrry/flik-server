package yunrry.flik.core.service.plan;

import com.sun.tools.javac.Main;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import yunrry.flik.core.domain.mapper.CategoryMapper;
import yunrry.flik.core.domain.model.*;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.core.domain.model.embedding.SpotSimilarity;
import yunrry.flik.core.domain.model.plan.CourseSlot;
import yunrry.flik.core.domain.model.plan.SlotType;
import yunrry.flik.core.domain.model.plan.TravelCourse;
import yunrry.flik.core.service.user.UserPreferenceService;
import yunrry.flik.ports.in.query.CourseQuery;
import yunrry.flik.ports.out.repository.UserRepository;
import yunrry.flik.ports.out.repository.UserSavedSpotRepository;
import yunrry.flik.ports.out.repository.SpotRepository;


import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TravelCourseRecommendationService {

    private final TravelPlannerService travelPlannerService;
    private final VectorSimilarityRecommendationService vectorSimilarityRecommendationService;
    private final UserSavedSpotRepository userSavedSpotRepository;
    private final UserPreferenceService userPreferenceService;
    private final SpotRepository spotRepository;
    private final CategoryMapper categoryMapper;

    /**
     * 사용자 맞춤 여행 코스 생성
     */
    public Mono<TravelCourse> generatePersonalizedTravelCourse(CourseQuery query) {
        String regionCode = query.getSelectedRegion();
        Long userId = query.getUserId();
        List<String> selectedCategoriesList = query.getSelectedCategories();
        String[] selectedCategories = selectedCategoriesList.toArray(new String[selectedCategoriesList.size()]);
        int days = query.getDays();

        return getUserSelectFrequency(userId, selectedCategories)
                .flatMap(frequencyMap -> {
                    // 1. 기본 코스 구조 생성
                    String[][] courseStructure = travelPlannerService.generateTravelCourse(
                            selectedCategories,
                            days,
                            frequencyMap
                    );
                    for(String[] day : courseStructure){
                        log.info(Arrays.toString(day));
                    }


                    // 2. 각 슬롯에 실제 장소 할당
                    return fillCourseWithRecommendedSpots(userId, courseStructure, regionCode)
                            .map(filledCourse -> TravelCourse.of(userId, days, filledCourse, selectedCategoriesList, regionCode));
                })
                .doOnSuccess(course -> log.info("Generated personalized course for user: {} with {} days", userId, days));
    }



    private Mono<Map<String, Integer>> getUserSelectFrequency(Long userId,  String[] selectedCategories) {
        return Mono.fromCallable(() -> {
            Map<String, Integer> frequencyMap = new HashMap<>();

            // 선택된 카테고리들에 대해 빈도 설정 (없으면 기본값 1)
            for (String category : selectedCategories) {
                String mainCategory = MainCategory.findByCode(category).getDisplayName();
                int count = userPreferenceService.getUserMainCategoryCount(userId, mainCategory);
                frequencyMap.put(category, count > 0 ? count : 1);
            }

            return frequencyMap;
        });
    }

    private Mono<CourseSlot[][]> fillCourseWithRecommendedSpots(Long userId,
                                                                String[][] courseStructure, String regionCode) {
        CourseSlot[][] filledCourse = new CourseSlot[courseStructure.length][courseStructure[0].length];

        return Mono.fromCallable(() -> {
            for (int day = 0; day < courseStructure.length; day++) {
                for (int slot = 0; slot < courseStructure[day].length; slot++) {
                    String slotType = courseStructure[day][slot];

                    if (slotType.isEmpty()) {
                        filledCourse[day][slot] = CourseSlot.empty(day, slot);
                    } else {
                        CourseSlot courseSlot = createCourseSlot(userId, day, slot, slotType, regionCode);
                        filledCourse[day][slot] = courseSlot;
                    }
                }
            }
            return filledCourse;
        });
    }



    private CourseSlot createCourseSlot(Long userId, int day, int slot, String slotType, String regionCode) {

        Boolean isContinue = slotType.contains("_continue");

        if (slotType.equals("restaurant")) {
            return createRestaurantSlot(userId, day, slot, regionCode);
        }

        if (slotType.equals("accommodation")) {
            return createAccommodationSlot(userId, day, slot, regionCode);
        }

        // 관광 슬롯 처리
        MainCategory mainCategory = getMainCategoryFromSlotType(slotType);
        if (mainCategory != null) {

            List<SpotSimilarity> recommendedSpots = findRecommendedSpotsBySubCategories(userId, 3, mainCategory, regionCode);

            log.info("Recommended spots for user {} in category {}: {} - {}", userId, mainCategory.getCode(),
                    recommendedSpots.stream().map(SpotSimilarity::spotId).collect(Collectors.toList()), recommendedSpots.stream().map(SpotSimilarity::similarity).collect(Collectors.toList()) );


            return CourseSlot.builder()
                    .day(day+1)
                    .slot(slot)
                    .slotType(SlotType.fromMainCategory(mainCategory))
                    .mainCategory(mainCategory)
                    .recommendedSpotIds(recommendedSpots.stream()
                            .map(SpotSimilarity::spotId)
                            .collect(Collectors.toList()))
                    .isContinue(isContinue)
                    .build();
        }

        return CourseSlot.empty(day, slot);
    }



    private CourseSlot createRestaurantSlot(Long userId, int day, int slot, String reginCode) {


        List<SpotSimilarity> restaurantSpots = findRecommendedSpotsBySubCategories(
                userId, 10, MainCategory.RESTAURANT, reginCode);

        log.info("Recommended spots for user {} in category {}: {} - {}", userId, MainCategory.RESTAURANT.getCode(),
                restaurantSpots.stream().map(SpotSimilarity::spotId).collect(Collectors.toList()), restaurantSpots.stream().map(SpotSimilarity::similarity).collect(Collectors.toList()) );


        return CourseSlot.builder()
                .day(day+1)
                .slot(slot)
                .slotType(SlotType.RESTAURANT)
                .mainCategory(MainCategory.RESTAURANT)
                .recommendedSpotIds(restaurantSpots.stream()
                        .map(SpotSimilarity::spotId)
                        .collect(Collectors.toList()))
                .build();
    }

    private CourseSlot createAccommodationSlot(Long userId, int day, int slot, String reginCode) {


        List<SpotSimilarity> accommodationSpots = findRecommendedSpotsBySubCategories(
                userId, 5, MainCategory.ACCOMMODATION, reginCode);

        log.info("Recommended spots for user {} in category {}: {} - {}", userId, MainCategory.ACCOMMODATION.getCode(),
                accommodationSpots.stream().map(SpotSimilarity::spotId).collect(Collectors.toList()), accommodationSpots.stream().map(SpotSimilarity::similarity).collect(Collectors.toList()) );

        return CourseSlot.builder()
                .day(day+1)
                .slot(slot)
                .slotType(SlotType.ACCOMMODATION)
                .mainCategory(MainCategory.ACCOMMODATION)
                .recommendedSpotIds(accommodationSpots.stream()
                        .map(SpotSimilarity::spotId)
                        .collect(Collectors.toList()))
                .build();
    }

    private MainCategory getMainCategoryFromSlotType(String slotType) {
        // _continue 제거
        String cleanType = slotType.replace("_continue", "");

        return MainCategory.findByCode(cleanType);
    }



    // 5. Updated findRecommendedSpotsBySubCategories
    private List<SpotSimilarity> findRecommendedSpotsBySubCategories(Long userId,
                                                                     int limit,
                                                                     MainCategory mainCategory,
                                                                     String regionCode) {

        List<String> subCategories = categoryMapper.getSubCategoryNames(mainCategory);

        // 1. 사용자가 저장한 장소들 조회
        List<Long> userSavedSpotIds = userSavedSpotRepository.findSpotIdsByUserId(userId);
//        List<Spot> candidateSpots;
        List<Long> candidateSpotIds = spotRepository.findIdsByIdsAndLabelDepth2InAndRegnCd(userSavedSpotIds, subCategories, regionCode);

//        if (userSavedSpotIds.isEmpty()) {
//            // 저장한 장소가 없으면 전체에서 선택
//            candidateSpots = spotRepository.findByLabelDepth2In(subCategories);
//        } else {
//            // 사용자가 저장한 장소들 중 해당 카테고리 우선, 부족하면 전체에서 보충
//            List<Spot> userSavedSpots = spotRepository.findByIdsAndLabelDepth2In(userSavedSpotIds, subCategories);
//
//            if (userSavedSpots.size() >= limit) {
//                log.info("userSavedSpotsize : {}", userSavedSpots.size());
//                candidateSpots = userSavedSpots;
//            } else {
//                candidateSpots = new ArrayList<>(userSavedSpots);
//                List<Spot> additionalSpots = spotRepository.findByLabelDepth2In(subCategories);
//                additionalSpots.removeIf(spot -> userSavedSpots.contains(spot));
//                candidateSpots.addAll(additionalSpots);
//            }
//        }

        if (candidateSpotIds.isEmpty()) {
            log.warn("No candidate spots found for user: {} in category: {}", userId, mainCategory);
            return List.of();
        }


        log.info("Candidate spot IDs for {}: {}", mainCategory, candidateSpotIds);
        // 2. 벡터 유사도 기반 추천
        return vectorSimilarityRecommendationService
                .findRecommendedSpotsByVectorSimilarity(userId, candidateSpotIds, mainCategory, limit);
    }



}
