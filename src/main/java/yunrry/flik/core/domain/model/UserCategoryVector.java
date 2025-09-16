package yunrry.flik.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCategoryVector {

    private Long id;
    private Long userId;
    private MainCategory category;
    private List<Double> preferenceVector;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean hasVector() {
        return preferenceVector != null && !preferenceVector.isEmpty();
    }

    public String getPreferenceVectorAsString() {
        return formatVector(preferenceVector);
    }

    private String formatVector(List<Double> vector) {
        if (vector == null || vector.isEmpty()) {
            return null;
        }
        return "[" + vector.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + "]";
    }
}