package yunrry.flik.core.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@RequiredArgsConstructor
@Builder
public class PostMetadata {
    private final String regionCode;
    private final BigDecimal rating;
    private final List<Long> spotIds;
    private final Long courseId;

    public static PostMetadata empty() {
        return PostMetadata.builder().build();
    }

    public static PostMetadata of(String regionCode, List<Long> spotIds, Long courseId) {
        return PostMetadata.builder()
                .regionCode(regionCode)
                .rating(BigDecimal.valueOf(0.0))
                .spotIds(spotIds)
                .courseId(courseId)
                .build();
    }

    public static PostMetadata updateRating(PostMetadata metadata, BigDecimal newRating) {
        return PostMetadata.builder()
                .regionCode(metadata.getRegionCode())
                .rating(newRating)
                .spotIds(metadata.getSpotIds())
                .courseId(metadata.getCourseId())
                .build();
    }
}