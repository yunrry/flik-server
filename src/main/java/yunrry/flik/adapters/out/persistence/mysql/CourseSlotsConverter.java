package yunrry.flik.adapters.out.persistence.mysql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import yunrry.flik.core.domain.model.plan.CourseSlot;

@Converter
@Slf4j
public class CourseSlotsConverter implements AttributeConverter<CourseSlot[][], String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(CourseSlot[][] courseSlots) {
        try {
            return objectMapper.writeValueAsString(courseSlots);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert CourseSlots to JSON", e);
            throw new RuntimeException("Failed to convert CourseSlots to JSON", e);
        }
    }

    @Override
    public CourseSlot[][] convertToEntityAttribute(String json) {
        try {
            return objectMapper.readValue(json, CourseSlot[][].class);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert JSON to CourseSlots: {}", json, e);
            throw new RuntimeException("Failed to convert JSON to CourseSlots", e);
        }
    }
}