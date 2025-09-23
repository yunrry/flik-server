package yunrry.flik.adapters.out.persistence.mysql.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class LongListConverter implements AttributeConverter<List<Long>, String> {

    @Override
    public String convertToDatabaseColumn(List<Long> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return attribute.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @Override
    public List<Long> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(dbData.split(","))
                .map(Long::parseLong)
                .toList();
    }
}