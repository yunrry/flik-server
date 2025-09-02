package yunrry.flik.core.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
@Builder
public class Restaurant {
    private final Long id;
    private final String name;
    private final String category;
    private final String description;
    private final String address;
    private final List<String> imageUrls;
    private final BigDecimal rating;
    private final Integer distance;
    private final LocalTime openTime;
    private final LocalTime closeTime;
    private final String dayOff;

    public boolean isOpenAt(LocalTime currentTime, String dayOfWeek) {
        if (dayOff != null && dayOff.equals(dayOfWeek)) {
            return false;
        }

        if (openTime == null || closeTime == null) {
            return false;
        }

        return !currentTime.isBefore(openTime) && !currentTime.isAfter(closeTime);
    }

    public Restaurant updateRating(BigDecimal newRating) {
        return Restaurant.builder()
                .id(this.id)
                .name(this.name)
                .category(this.category)
                .description(this.description)
                .address(this.address)
                .imageUrls(this.imageUrls)
                .rating(newRating)
                .distance(this.distance)
                .openTime(this.openTime)
                .closeTime(this.closeTime)
                .dayOff(this.dayOff)
                .build();
    }


    public Restaurant updateDistance(Integer newDistance) {
        return Restaurant.builder()
                .id(this.id)
                .name(this.name)
                .category(this.category)
                .description(this.description)
                .address(this.address)
                .imageUrls(this.imageUrls)
                .rating(this.rating)
                .distance(newDistance)
                .openTime(this.openTime)
                .closeTime(this.closeTime)
                .dayOff(this.dayOff)
                .build();
    }
}