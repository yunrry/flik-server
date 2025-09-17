package yunrry.flik.core.service.plan;

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
import yunrry.flik.core.service.user.UserCategoryVectorService;
import yunrry.flik.core.service.user.UserPreferenceService;
import yunrry.flik.ports.out.repository.UserCategoryPreferenceRepository;
import yunrry.flik.ports.out.repository.UserSavedSpotRepository;
import yunrry.flik.ports.out.repository.SpotRepository;


import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TravelCourseRecommendationService {

    private final TravelPlannerService travelPlannerService;
//    private final TravelPlanRecommendationService travelPlanRecommendationService;
    private final VectorSimilarityRecommendationService vectorSimilarityRecommendationService;
    private final UserSavedSpotRepository userSavedSpotRepository;
    private final UserCategoryPreferenceRepository userCategoryPreferenceRepository;
    private final UserPreferenceService userPreferenceService;
    private final UserCategoryVectorService userCategoryVectorService;
    private final SpotRepository spotRepository;
    private final CategoryMapper categoryMapper;

    /**
     * 사용자 맞춤 여행 코스 생성
     */
    public Mono<TravelCourse> generatePersonalizedTravelCourse(Long userId,
                                                               String[] selectedCategories,
                                                               int days,
                                                               double locationWeight,
                                                               double tagWeight) {
        return getUserSelectFrequency(userId, selectedCategories)
                .flatMap(frequencyMap -> {
                    // 1. 기본 코스 구조 생성
                    String[][] courseStructure = travelPlannerService.generateTravelCourse(
                            selectedCategories,
                            days,
                            frequencyMap
                    );

                    // 2. 각 슬롯에 실제 장소 할당
                    return fillCourseWithRecommendedSpots(userId, courseStructure, locationWeight, tagWeight)
                            .map(filledCourse -> TravelCourse.of(userId, days, filledCourse));
                })
                .doOnSuccess(course -> log.info("Generated personalized course for user: {} with {} days", userId, days));
    }



    private Mono<Map<String, Integer>> getUserSelectFrequency(Long userId,  String[] selectedCategories) {
        return Mono.fromCallable(() -> {
            Map<String, Integer> frequencyMap = new HashMap<>();

            // 선택된 카테고리들에 대해 빈도 설정 (없으면 기본값 1)
            for (String category : selectedCategories) {
                int count = userPreferenceService.getUserMainCategoryCount(userId, category);
                frequencyMap.put(category, count > 0 ? count : 1);
            }

            return frequencyMap;
        });
    }

    private Mono<CourseSlot[][]> fillCourseWithRecommendedSpots(Long userId,
                                                                String[][] courseStructure,
                                                                double locationWeight,
                                                                double tagWeight) {
        CourseSlot[][] filledCourse = new CourseSlot[courseStructure.length][courseStructure[0].length];

        return Mono.fromCallable(() -> {
            for (int day = 0; day < courseStructure.length; day++) {
                for (int slot = 0; slot < courseStructure[day].length; slot++) {
                    String slotType = courseStructure[day][slot];

                    if (slotType.isEmpty()) {
                        filledCourse[day][slot] = CourseSlot.empty(day, slot);
                    } else {
                        CourseSlot courseSlot = createCourseSlot(userId, day, slot, slotType, locationWeight, tagWeight);
                        filledCourse[day][slot] = courseSlot;
                    }
                }
            }
            return filledCourse;
        });
    }



    private CourseSlot createCourseSlot(Long userId, int day, int slot, String slotType,
                                        double locationWeight, double tagWeight) {

        Boolean isContinue = slotType.contains("_continue");

        if (slotType.equals("restaurant")) {
            return createRestaurantSlot(userId, day, slot, locationWeight, tagWeight);
        }

        if (slotType.equals("accommodation")) {
            return createAccommodationSlot(userId, day, slot, locationWeight, tagWeight);
        }

        // 관광 슬롯 처리
        MainCategory mainCategory = getMainCategoryFromSlotType(slotType);
        if (mainCategory != null) {

            List<SpotSimilarity> recommendedSpots = findRecommendedSpotsBySubCategories(userId, locationWeight, tagWeight, 3, mainCategory);

            log.info("Recommended spots for user {} in category {}: {} - {}", userId, mainCategory.getCode(),
                    recommendedSpots.stream().map(SpotSimilarity::spotId).collect(Collectors.toList()), recommendedSpots.stream().map(SpotSimilarity::similarity).collect(Collectors.toList()) );


            return CourseSlot.builder()
                    .day(day)
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



    private CourseSlot createRestaurantSlot(Long userId, int day, int slot, double locationWeight, double tagWeight) {


        List<SpotSimilarity> restaurantSpots = findRecommendedSpotsBySubCategories(
                userId, locationWeight, tagWeight, 3, MainCategory.RESTAURANT);

        log.info("Recommended spots for user {} in category {}: {} - {}", userId, MainCategory.RESTAURANT.getCode(),
                restaurantSpots.stream().map(SpotSimilarity::spotId).collect(Collectors.toList()), restaurantSpots.stream().map(SpotSimilarity::similarity).collect(Collectors.toList()) );


        return CourseSlot.builder()
                .day(day)
                .slot(slot)
                .slotType(SlotType.RESTAURANT)
                .mainCategory(MainCategory.RESTAURANT)
                .recommendedSpotIds(restaurantSpots.stream()
                        .map(SpotSimilarity::spotId)
                        .collect(Collectors.toList()))
                .build();
    }

    private CourseSlot createAccommodationSlot(Long userId, int day, int slot, double locationWeight, double tagWeight) {


        List<SpotSimilarity> accommodationSpots = findRecommendedSpotsBySubCategories(
                userId, locationWeight, tagWeight, 3, MainCategory.ACCOMMODATION);

        log.info("Recommended spots for user {} in category {}: {} - {}", userId, MainCategory.ACCOMMODATION.getCode(),
                accommodationSpots.stream().map(SpotSimilarity::spotId).collect(Collectors.toList()), accommodationSpots.stream().map(SpotSimilarity::similarity).collect(Collectors.toList()) );

        return CourseSlot.builder()
                .day(day)
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
                                                                     double locationWeight,
                                                                     double tagWeight,
                                                                     int limit,
                                                                     MainCategory mainCategory) {

        List<String> subCategories = categoryMapper.getSubCategoryNames(mainCategory);

        // 1. 사용자가 저장한 장소들 조회
        List<Long> userSavedSpotIds = userSavedSpotRepository.findSpotIdsByUserId(userId);
        List<Spot> candidateSpots;

        if (userSavedSpotIds.isEmpty()) {
            // 저장한 장소가 없으면 전체에서 선택
            candidateSpots = spotRepository.findByLabelDepth2In(subCategories);
        } else {
            // 사용자가 저장한 장소들 중 해당 카테고리 우선, 부족하면 전체에서 보충
            List<Spot> userSavedSpots = spotRepository.findByIdsAndLabelDepth2In(userSavedSpotIds, subCategories);

            if (userSavedSpots.size() >= limit) {
                candidateSpots = userSavedSpots;
            } else {
                candidateSpots = new ArrayList<>(userSavedSpots);
                List<Spot> additionalSpots = spotRepository.findByLabelDepth2In(subCategories);
                additionalSpots.removeIf(spot -> userSavedSpots.contains(spot));
                candidateSpots.addAll(additionalSpots);
            }
        }

        if (candidateSpots.isEmpty()) {
            return List.of();
        }

        List<Long> candidateSpotIds = candidateSpots.stream()
                .map(Spot::getId)
                .collect(Collectors.toList());

        // 2. 벡터 유사도 기반 추천
        return vectorSimilarityRecommendationService
                .findRecommendedSpotsByVectorSimilarity(userId, candidateSpotIds, mainCategory, limit);
    }



}
