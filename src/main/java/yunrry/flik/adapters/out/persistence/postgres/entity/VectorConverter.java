package yunrry.flik.adapters.out.persistence.postgres.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class VectorConverter implements AttributeConverter<List<Double>, String> {
    @Override
    public String convertToDatabaseColumn(List<Double> attribute) {
        if (attribute == null || attribute.isEmpty()) return null;
        return "[" + attribute.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + "]";
    }

    @Override
    public List<Double> convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String cleaned = dbData.replace("[", "").replace("]", "");
        return Arrays.stream(cleaned.split(","))
                .map(String::trim)
                .map(Double::parseDouble)
                .collect(Collectors.toList());
    }
}