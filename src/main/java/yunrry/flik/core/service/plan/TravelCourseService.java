package yunrry.flik.core.service.plan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yunrry.flik.adapters.in.dto.TravelCourseUpdateRequest;
import yunrry.flik.core.domain.model.plan.CourseSlot;
import yunrry.flik.core.domain.model.plan.TravelCourse;
import yunrry.flik.core.service.MetricsService;
import yunrry.flik.ports.in.query.TravelCourseQuery;
import yunrry.flik.ports.in.usecase.TravelCourseUseCase;
import yunrry.flik.ports.out.repository.TravelCourseRepository;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class TravelCourseService implements TravelCourseUseCase {

    private final TravelCourseRepository travelCourseRepository;
    private final MetricsService metricsService;

    @Override
    public TravelCourse getTravelCourse(Long courseId) {

        return travelCourseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Travel course not found with id: " + courseId));
    }

    @Override
    public List<TravelCourse> getTravelCoursesByUserId(Long userId) {
        return travelCourseRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public TravelCourse updateTravelCourse(Long id, TravelCourseUpdateRequest request) {
        TravelCourse existing = travelCourseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("TravelCourse not found with id: " + id));

        // 코스 슬롯 변경 추적
        if (request.getCourseSlots() != null) {
            trackCourseSlotChanges(existing.getCourseSlots(), request.getCourseSlots());
        }

        // totalDistance 업데이트
        if (request.getTotalDistance() != null) {
            existing.updateTotalDistance(request.getTotalDistance());
        }

        if(request.getName() != null) {
            existing.updateName(request.getName());
        }

        // courseSlots 업데이트
        if (request.getCourseSlots() != null) {
            existing = TravelCourse.builder()
                    .id(existing.getId())
                    .userId(existing.getUserId())
                    .days(existing.getDays())
                    .totalDistance(existing.getTotalDistance())
                    .courseSlots(request.getCourseSlots())
                    .createdAt(existing.getCreatedAt())
                    .courseType(existing.getCourseType())
                    .regionCode(request.getRegionCode() != null ? request.getRegionCode() : existing.getRegionCode())
                    .selectedCategories(request.getSelectedCategories() != null ? request.getSelectedCategories() : existing.getSelectedCategories())
                    .isPublic(request.getIsPublic() != null ? request.getIsPublic() : existing.getIsPublic())
                    .build();
        } else {
            // regionCode, selectedCategories만 업데이트
            if (request.getRegionCode() != null) {
                existing = TravelCourse.builder()
                        .id(existing.getId())
                        .userId(existing.getUserId())
                        .name(existing.getName())
                        .days(existing.getDays())
                        .totalDistance(existing.getTotalDistance())
                        .courseSlots(existing.getCourseSlots())
                        .createdAt(existing.getCreatedAt())
                        .courseType(existing.getCourseType())
                        .regionCode(request.getRegionCode())
                        .selectedCategories(request.getSelectedCategories() != null ? request.getSelectedCategories() : existing.getSelectedCategories())
                        .isPublic(request.getIsPublic() != null ? request.getIsPublic() : existing.getIsPublic())
                        .build();
            }
        }

        return travelCourseRepository.save(existing);
    }

    @Override
    @Transactional
    public TravelCourse updateCourseVisibility(Long id, Boolean isPublic) {
        TravelCourse existing = travelCourseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("TravelCourse not found with id: " + id));

        existing = TravelCourse.builder()
                .id(existing.getId())
                .userId(existing.getUserId())
                .name(existing.getName())
                .days(existing.getDays())
                .totalDistance(existing.getTotalDistance())
                .courseSlots(existing.getCourseSlots())
                .createdAt(existing.getCreatedAt())
                .courseType(existing.getCourseType())
                .regionCode(existing.getRegionCode())
                .selectedCategories(existing.getSelectedCategories())
                .isPublic(isPublic)
                .build();

        return travelCourseRepository.save(existing);
    }


    @Override
    @Transactional
    public void deleteTravelCourse(Long courseId) {
        if (!travelCourseRepository.findById(courseId).isPresent()) {
            throw new IllegalArgumentException("Travel course not found with id: " + courseId);
        }
        travelCourseRepository.deleteById(courseId);
    }


    @Override
    public List<TravelCourse> getTravelCoursesByRegionCode(String regionCode) {
        log.info("Finding travel courses by regionCode: {}", regionCode);
        return travelCourseRepository.findByRegionCode(regionCode);
    }


    @Override
    public List<TravelCourse> getTravelCoursesByRegionPrefix(String regionPrefix) {
        return travelCourseRepository.findByRegionCodePrefix(regionPrefix);
    }


    private void trackCourseSlotChanges(CourseSlot[][] oldSlots, CourseSlot[][] newSlots) {

        Set<Long> oldSpotIds = extractSpotIdsFrom2DArray(oldSlots);
        Set<Long> newSpotIds = extractSpotIdsFrom2DArray(newSlots);

        // 삭제된 장소들
        Set<Long> deletedSpotIds = oldSpotIds.stream()
                .filter(id -> !newSpotIds.contains(id))
                .collect(Collectors.toSet());

        // 추가된 장소들
        Set<Long> addedSpotIds = newSpotIds.stream()
                .filter(id -> !oldSpotIds.contains(id))
                .collect(Collectors.toSet());

        if (!deletedSpotIds.isEmpty()) {
            log.info("Tracking deletion of {} spots", deletedSpotIds.size());
            metricsService.recordSpotDeletionFromCourse(deletedSpotIds.size());
        }

        if (!addedSpotIds.isEmpty()) {
            log.info("Tracking addition of {} spots", addedSpotIds.size());
            metricsService.recordSpotAdditionToCourse(addedSpotIds.size());
        }
    }

    private Set<Long> extractSpotIdsFrom2DArray(CourseSlot[][] slots) {
        Set<Long> spotIds = new HashSet<>();

        if (slots != null) {
            for (CourseSlot[] daySlots : slots) {
                for (CourseSlot slot : daySlots) {
                    if (slot != null && slot.getSelectedSpotId() != null) {
                        spotIds.add(slot.getSelectedSpotId());
                    }
                }
            }
        }

        return spotIds;
    }
}
