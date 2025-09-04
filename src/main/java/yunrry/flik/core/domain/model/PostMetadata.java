package yunrry.flik.core.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
@Builder
public class PostMetadata {
    private final String restaurantName;
    private final String location;
    private final BigDecimal rating;
    private final Long restaurantId;

    public static PostMetadata empty() {
        return PostMetadata.builder().build();
    }

    public static PostMetadata of(String restaurantName, String location, BigDecimal rating) {
        return PostMetadata.builder()
                .restaurantName(restaurantName)
                .location(location)
                .rating(rating)
                .build();
    }
}