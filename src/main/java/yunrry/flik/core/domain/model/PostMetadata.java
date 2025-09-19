package yunrry.flik.core.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
@Builder
public class PostMetadata {
    private final String spotName;
    private final String location;
    private final BigDecimal rating;
    private final Long spotId;
    private final Long courseId;

    public static PostMetadata empty() {
        return PostMetadata.builder().build();
    }

    public static PostMetadata of(String spotName, String location, BigDecimal rating, Long spotId, Long courseId) {
        return PostMetadata.builder()
                .spotName(spotName)
                .location(location)
                .rating(rating)
                .spotId(spotId)
                .courseId(courseId)
                .build();
    }
}