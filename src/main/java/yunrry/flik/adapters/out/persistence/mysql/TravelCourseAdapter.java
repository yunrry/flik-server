package yunrry.flik.adapters.out.persistence.mysql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import yunrry.flik.adapters.out.persistence.mysql.entity.TravelCourseEntity;

import yunrry.flik.adapters.out.persistence.mysql.repository.TravelCourseJpaRepository;
import yunrry.flik.core.domain.model.plan.CourseSlot;
import yunrry.flik.core.domain.model.plan.CourseType;
import yunrry.flik.core.domain.model.plan.TravelCourse;
import yunrry.flik.ports.out.repository.TravelCourseRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TravelCourseAdapter implements TravelCourseRepository {

    private final TravelCourseJpaRepository travelCourseJpaRepository;
    private final ObjectMapper objectMapper;

    @Override
    public TravelCourse save(TravelCourse travelCourse) {
        try {
            String courseSlotsJson = objectMapper.writeValueAsString(travelCourse.getCourseSlots());

            TravelCourseEntity entity = TravelCourseEntity.from(
                    travelCourse,
                    courseSlotsJson
            );

            TravelCourseEntity savedEntity = travelCourseJpaRepository.save(entity);

            // Convert back to domain with deserialized course slots
            CourseSlot[][] courseSlots = deserializeCourseSlots(savedEntity.getCourseSlotsJson());
            return savedEntity.toDomain(courseSlots);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize course slots", e);
            throw new RuntimeException("Failed to save travel course", e);
        }
    }

    @Override
    public Optional<TravelCourse> findById(Long id) {
        return travelCourseJpaRepository.findById(id)
                .map(this::entityToDomain);
    }

    @Override
    public List<TravelCourse> findByUserId(Long userId) {
        return travelCourseJpaRepository.findByUserId(userId)
                .stream()
                .map(this::entityToDomain)
                .toList();
    }

    @Override
    public List<TravelCourse> findByUserIdAndCourseType(Long userId, CourseType courseType) {
        return travelCourseJpaRepository.findByUserIdAndCourseType(userId, courseType)
                .stream()
                .map(this::entityToDomain)
                .toList();
    }

    @Override
    public List<TravelCourse> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime createdAt) {
        return travelCourseJpaRepository.findByUserIdAndCreatedAtAfter(userId, createdAt)
                .stream()
                .map(this::entityToDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        travelCourseJpaRepository.deleteById(id);
    }

    @Override
    public void deleteByUserId(Long userId) {
        travelCourseJpaRepository.deleteByUserId(userId);
    }

    @Override
    public boolean existsByUserIdAndCourseType(Long userId, CourseType courseType) {
        return travelCourseJpaRepository.existsByUserIdAndCourseType(userId, courseType);
    }

    @Override
    public List<TravelCourse> findByRegionCode(String regionCode) {
        return travelCourseJpaRepository.findByRegionCode(regionCode).stream()
                .map(this::entityToDomain)
                .collect(Collectors.toList());
    }

    private TravelCourse entityToDomain(TravelCourseEntity entity) {
        CourseSlot[][] courseSlots = deserializeCourseSlots(entity.getCourseSlotsJson());
        return entity.toDomain(courseSlots);
    }

    private CourseSlot[][] deserializeCourseSlots(String courseSlotsJson) {
        try {
            return objectMapper.readValue(courseSlotsJson, CourseSlot[][].class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize course slots: {}", courseSlotsJson, e);
            throw new RuntimeException("Failed to deserialize course slots", e);
        }
    }
}