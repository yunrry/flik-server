package yunrry.flik.core.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import yunrry.flik.core.domain.exception.FestivalRunningTimeNullException;
import yunrry.flik.core.domain.exception.SpotRunningTimeNullException;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
@Builder
public class Festival {

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

        if (openTime != null && closeTime != null) {
            return (!currentTime.isBefore(openTime) && !currentTime.isAfter(closeTime));

        }else{
            throw new FestivalRunningTimeNullException(id);
        }
    }

    public Festival updateRating(BigDecimal newRating) {
        return Festival.builder()
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


    public Festival updateDistance(Integer newDistance) {
        return Festival.builder()
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
