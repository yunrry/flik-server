package yunrry.flik.core.service.plan;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import yunrry.flik.core.domain.model.MainCategory;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.core.domain.model.plan.CourseSlot;
import yunrry.flik.core.domain.model.plan.SlotType;
import yunrry.flik.core.domain.model.plan.TravelCourse;
import yunrry.flik.core.domain.testfixture.SpotTestFixture;
import yunrry.flik.core.service.MetricsService;
import yunrry.flik.ports.in.query.CourseQuery;
import yunrry.flik.ports.out.repository.SpotRepository;
import yunrry.flik.ports.out.repository.TravelCourseRepository;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTravelCourseServiceTest {

    @InjectMocks
    private CreateTravelCourseService service;

    @Mock
    private TravelCourseRecommendationService travelCourseRecommendationService;

    @Mock
    private MetricsService metricsService;

    @Mock
    private SpotRepository spotRepository;

    @Mock
    private TravelCourseRepository travelCourseRepository;

    private CourseQuery query;
    private TravelCourse mockCourse;
    private Map<Long, Spot> spotCache;
    private SpotTestFixture spotTestFixture;

    @BeforeEach
    void setUp() {
        query = CourseQuery.builder()
                .userId(1L)
                .selectedRegion("11110")
                .selectedCategories(Arrays.asList("nature", "cafe"))
                .days(1)
                .build();

        spotCache = createSpotCache();
    }

    // ========== 1. 정상 생성 테스트 ==========

    @Test
    @DisplayName("TC-001: 1일 코스 생성")
    void testCreateOneDayCourse() {
        // Given
        mockCourse = createRealCourse(1);
        when(travelCourseRecommendationService.generatePersonalizedTravelCourse(query))
                .thenReturn(mockCourse);
        when(spotRepository.findAllByIds(anyList()))
                .thenReturn(new ArrayList<>(spotCache.values()));
        when(travelCourseRepository.save(any(TravelCourse.class)))
                .thenReturn(mockCourse);

        // When
        TravelCourse result = service.create(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getDays());
        verify(travelCourseRecommendationService).generatePersonalizedTravelCourse(query);
        verify(spotRepository).findAllByIds(anyList());
        verify(travelCourseRepository).save(any(TravelCourse.class));
    }

    @Test
    @DisplayName("TC-002: 2박3일 코스 생성")
    void testCreateThreeDayCourse() {
        // Given
        query = CourseQuery.builder()
                .userId(1L)
                .selectedRegion("11")
                .selectedCategories(Arrays.asList("nature"))
                .days(3)
                .build();

        mockCourse = createRealCourse(3);
        when(travelCourseRecommendationService.generatePersonalizedTravelCourse(query))
                .thenReturn(mockCourse);
        when(spotRepository.findAllByIds(anyList()))
                .thenReturn(new ArrayList<>(spotCache.values()));
        when(travelCourseRepository.save(any(TravelCourse.class)))
                .thenReturn(mockCourse);

        // When
        TravelCourse result = service.create(query);

        // Then
        assertEquals(3, result.getDays());
    }

    @Test
    @DisplayName("TC-003: 총 이동거리 계산")
    void testTotalDistanceCalculation() {
        // Given
        mockCourse = createRealCourse(1);
        when(travelCourseRecommendationService.generatePersonalizedTravelCourse(query))
                .thenReturn(mockCourse);
        when(spotRepository.findAllByIds(anyList()))
                .thenReturn(new ArrayList<>(spotCache.values()));
        when(travelCourseRepository.save(any(TravelCourse.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TravelCourse result = service.create(query);

        // Then
        assertTrue(result.getTotalDistance() >= 0);
    }

    @Test
    @DisplayName("TC-004: DB 저장 확인")
    void testSaveToDatabase() {
        // Given
        TravelCourse inputCourse = createRealCourse(1);

        TravelCourse savedCourse = TravelCourse.builder()
                .id(100L)
                .userId(inputCourse.getUserId())
                .days(inputCourse.getDays())
                .courseSlots(inputCourse.getCourseSlots())
                .selectedCategories(inputCourse.getSelectedCategories())
                .regionCode(inputCourse.getRegionCode())
                .courseType(inputCourse.getCourseType())
                .totalDistance(inputCourse.getTotalDistance())
                .createdAt(inputCourse.getCreatedAt())
                .build();

        when(travelCourseRecommendationService.generatePersonalizedTravelCourse(query))
                .thenReturn(inputCourse);
        when(spotRepository.findAllByIds(anyList()))
                .thenReturn(new ArrayList<>(spotCache.values()));
        when(travelCourseRepository.save(any(TravelCourse.class)))
                .thenReturn(savedCourse);

        // When
        TravelCourse result = service.create(query);

        // Then
        assertNotNull(result.getId());
        assertEquals(100L, result.getId());
    }

    // ========== 2. 장소 선택 알고리즘 ==========

    @Test
    @DisplayName("TC-101: 첫 2슬롯 최근접 쌍 선택")
    void testFirstTwoSlotsClosestPair() {
        // Given
        mockCourse = createRealCourse(1);
        when(travelCourseRecommendationService.generatePersonalizedTravelCourse(query))
                .thenReturn(mockCourse);
        when(spotRepository.findAllByIds(anyList()))
                .thenReturn(new ArrayList<>(spotCache.values()));
        when(travelCourseRepository.save(any(TravelCourse.class)))
                .thenReturn(mockCourse);

        // When
        service.create(query);

        // Then
        CourseSlot slot0 = mockCourse.getSlot(0, 0);
        CourseSlot slot1 = mockCourse.getSlot(0, 1);
        assertNotNull(slot0.getSelectedSpotId());
        assertNotNull(slot1.getSelectedSpotId());
        assertNotEquals(slot0.getSelectedSpotId(), slot1.getSelectedSpotId());
    }

    @Test
    @DisplayName("TC-103: 중복 제거 - 전역 중복 없음")
    void testNoDuplicateSelection() {
        // Given
        query = CourseQuery.builder()
                .userId(1L)
                .selectedRegion("11")
                .selectedCategories(Arrays.asList("nature"))
                .days(2)
                .build();

        TravelCourse inputCourse = createRealCourse(2);
        when(travelCourseRecommendationService.generatePersonalizedTravelCourse(any()))
                .thenReturn(inputCourse);
        when(spotRepository.findAllByIds(anyList()))
                .thenReturn(new ArrayList<>(spotCache.values()));
        when(travelCourseRepository.save(any(TravelCourse.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.create(query);

        // Then
        Set<Long> selectedSpots = new HashSet<>();
        for (int day = 0; day < 2; day++) {
            int slotsInDay = (day == 1) ? 5 : 6; // 마지막 날은 5슬롯
            for (int slot = 0; slot < slotsInDay; slot++) {
                Long spotId = inputCourse.getSlot(day, slot).getSelectedSpotId();
                if (spotId != null) {
                    assertFalse(selectedSpots.contains(spotId), "중복 선택: " + spotId);
                    selectedSpots.add(spotId);
                }
            }
        }
    }
    // ========== 3. 캐싱 및 성능 ==========

    @Test
    @DisplayName("TC-201: 일괄 조회 1회 호출")
    void testBulkLoadOnce() {
        // Given
        mockCourse = createRealCourse(1);
        when(travelCourseRecommendationService.generatePersonalizedTravelCourse(query))
                .thenReturn(mockCourse);
        when(spotRepository.findAllByIds(anyList()))
                .thenReturn(new ArrayList<>(spotCache.values()));
        when(travelCourseRepository.save(any(TravelCourse.class)))
                .thenReturn(mockCourse);

        // When
        service.create(query);

        // Then
        verify(spotRepository, times(1)).findAllByIds(anyList());
    }

    @Test
    @DisplayName("TC-202: 캐시 사용 - findById 호출 없음")
    void testNoSingleFindById() {
        // Given
        mockCourse = createRealCourse(1);
        when(travelCourseRecommendationService.generatePersonalizedTravelCourse(query))
                .thenReturn(mockCourse);
        when(spotRepository.findAllByIds(anyList()))
                .thenReturn(new ArrayList<>(spotCache.values()));
        when(travelCourseRepository.save(any(TravelCourse.class)))
                .thenReturn(mockCourse);

        // When
        service.create(query);

        // Then
        verify(spotRepository, never()).findById(anyLong());
    }

    // ========== 4. 예외 처리 ==========

    @Test
    @DisplayName("TC-301: 빈 슬롯 처리")
    void testEmptySlotHandling() {
        // Given
        TravelCourse emptyCourse = createEmptyCourse(1);
        when(travelCourseRecommendationService.generatePersonalizedTravelCourse(query))
                .thenReturn(emptyCourse);
        // spotRepository stubbing 제거 - 빈 코스는 조회 안 함
        when(travelCourseRepository.save(any(TravelCourse.class)))
                .thenReturn(emptyCourse);

        // When
        TravelCourse result = service.create(query);

        // Then
        assertNotNull(result);
        assertEquals(0.0, result.getTotalDistance());
    }

    @Test
    @DisplayName("TC-303: 추천 실패 - RuntimeException")
    void testRecommendationFailure() {
        // Given
        when(travelCourseRecommendationService.generatePersonalizedTravelCourse(query))
                .thenThrow(new RuntimeException("추천 실패"));

        // When & Then
        assertThrows(RuntimeException.class, () -> service.create(query));
    }

    @Test
    @DisplayName("TC-304: 저장 실패 - 예외 전파")
    void testSaveFailure() {
        // Given
        mockCourse = createRealCourse(1);
        when(travelCourseRecommendationService.generatePersonalizedTravelCourse(query))
                .thenReturn(mockCourse);
        when(spotRepository.findAllByIds(anyList()))
                .thenReturn(new ArrayList<>(spotCache.values()));
        when(travelCourseRepository.save(any(TravelCourse.class)))
                .thenThrow(new RuntimeException("DB 저장 실패"));

        // When & Then
        assertThrows(RuntimeException.class, () -> service.create(query));
    }

    // ========== 5. 거리 계산 ==========

    @Test
    @DisplayName("TC-401: Haversine 정확도")
    void testHaversineAccuracy() {
        // Given - 서울시청 to 남산타워, 실제 거리 약 1.9km
        TravelCourse course = createCourseWithTwoSpots();

        when(travelCourseRecommendationService.generatePersonalizedTravelCourse(query))
                .thenReturn(course);

        List<Spot> spots = Arrays.asList(
                createSpot(101L, new BigDecimal("37.5665"), new BigDecimal("126.9780")),
                createSpot(102L, new BigDecimal("37.5512"), new BigDecimal("126.9882"))
        );
        when(spotRepository.findAllByIds(anyList())).thenReturn(spots);
        when(travelCourseRepository.save(any(TravelCourse.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TravelCourse result = service.create(query);

        // Then
        assertTrue(result.getTotalDistance() > 1.5 && result.getTotalDistance() < 2.5,
                "Expected ~1.9km, got " + result.getTotalDistance());
    }

    @Test
    @DisplayName("TC-402: 일자 내 거리 합산")
    void testWithinDayDistance() {
        // Given
        mockCourse = createRealCourse(1);
        when(travelCourseRecommendationService.generatePersonalizedTravelCourse(query))
                .thenReturn(mockCourse);
        when(spotRepository.findAllByIds(anyList()))
                .thenReturn(new ArrayList<>(spotCache.values()));
        when(travelCourseRepository.save(any(TravelCourse.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TravelCourse result = service.create(query);

        // Then
        assertTrue(result.getTotalDistance() > 0);
    }

    // ========== 6. 경계 조건 ==========

    @Test
    @DisplayName("TC-501: 추천 없는 슬롯")
    void testNoRecommendations() {
        // Given
        TravelCourse emptyCourse = createEmptyCourse(1);
        when(travelCourseRecommendationService.generatePersonalizedTravelCourse(query))
                .thenReturn(emptyCourse);
        // spotRepository stubbing 제거
        when(travelCourseRepository.save(any(TravelCourse.class)))
                .thenReturn(emptyCourse);

        // When
        TravelCourse result = service.create(query);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("TC-503: 빈 일자 - 거리 0")
    void testEmptyDay() {
        // Given
        TravelCourse emptyCourse = createEmptyCourse(1);
        when(travelCourseRecommendationService.generatePersonalizedTravelCourse(query))
                .thenReturn(emptyCourse);
        when(travelCourseRepository.save(any(TravelCourse.class)))
                .thenReturn(emptyCourse);

        // When
        TravelCourse result = service.create(query);

        // Then
        assertEquals(0.0, result.getTotalDistance());
    }

    // ========== Helper Methods ==========

    private Map<Long, Spot> createSpotCache() {
        Map<Long, Spot> cache = new HashMap<>();
        cache.put(101L, createSpot(101L, new BigDecimal("37.5665"), new BigDecimal("126.9780")));
        cache.put(102L, createSpot(102L, new BigDecimal("37.5512"), new BigDecimal("126.9882")));
        cache.put(103L, createSpot(103L, new BigDecimal("37.5700"), new BigDecimal("126.9800")));
        cache.put(104L, createSpot(104L, new BigDecimal("37.5600"), new BigDecimal("126.9900")));
        cache.put(105L, createSpot(105L, new BigDecimal("37.5550"), new BigDecimal("126.9850")));
        return cache;
    }

    private Spot createSpot(Long id, BigDecimal lat, BigDecimal lon) {

        return spotTestFixture.createTestSpotForDistance(id, lat, lon);
    }

    private TravelCourse createRealCourse(int days) {
        CourseSlot[][] slots = new CourseSlot[days][];

        for (int day = 0; day < days; day++) {
            int slotCount = (day == days - 1 && days > 1) ? 5 : 6;
            slots[day] = new CourseSlot[slotCount];

            for (int slot = 0; slot < slotCount; slot++) {
                SlotType slotType = determineSlotType(slot);
                slots[day][slot] = CourseSlot.of(
                        day,
                        slot,
                        slotType,
                        MainCategory.NATURE,
                        Arrays.asList(101L, 102L, 103L, 104L, 105L)
                );
            }
        }

        return TravelCourse.of(1L, days, slots, Arrays.asList("nature"), "11");
    }

    private TravelCourse createCourseWithTwoSpots() {
        CourseSlot[][] slots = new CourseSlot[1][6];
        slots[0][0] = CourseSlot.of(0, 0, SlotType.TOURISM, MainCategory.NATURE, List.of(101L));
        slots[0][1] = CourseSlot.of(0, 1, SlotType.TOURISM, MainCategory.NATURE, List.of(102L));
        // 나머지 빈 슬롯
        for (int i = 2; i < 6; i++) {
            slots[0][i] = CourseSlot.of(0, i, determineSlotType(i), MainCategory.NATURE, Collections.emptyList());
        }
        return TravelCourse.of(1L, 1, slots, List.of("nature"), "11");
    }

    private TravelCourse createEmptyCourse(int days) {
        CourseSlot[][] slots = new CourseSlot[days][6];

        for (int day = 0; day < days; day++) {
            for (int slot = 0; slot < 6; slot++) {
                SlotType slotType = determineSlotType(slot);
                slots[day][slot] = CourseSlot.of(day, slot, slotType, MainCategory.NATURE, Collections.emptyList());
            }
        }

        return TravelCourse.of(1L, days, slots, Arrays.asList("nature"), "11");
    }

    private SlotType determineSlotType(int slot) {
        return switch (slot) {
            case 2, 4 -> SlotType.RESTAURANT;
            case 5 -> SlotType.ACCOMMODATION;
            default -> SlotType.TOURISM;
        };
    }

}