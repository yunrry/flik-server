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

    public static PostMetadata empty() {
        return PostMetadata.builder().build();
    }

    public static PostMetadata of(String spotName, String location, BigDecimal rating) {
        return PostMetadata.builder()
                .spotName(spotName)
                .location(location)
                .rating(rating)
                .build();
    }
}