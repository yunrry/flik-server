package yunrry.flik.core.domain.model.card;


import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import yunrry.flik.core.domain.exception.SpotRunningTimeNullException;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
public abstract class Spot {
    public final Long id;
    public final String name;
    public final String contentTypeId;
    public final String contentId;
    public final String category;
    public final String description;
    public final String address;
    public final String regnCd;
    public final String signguCd;
    public final BigDecimal latitude;
    public final BigDecimal longitude;
    public final List<String> imageUrls;
    public final String info;
    public final BigDecimal rating;
    public final String googlePlaceId;
    public final Integer reviewCount;
    public final String tag1;
    public final String tag2;
    public final String tag3;
    public final String tags;
    public final String labelDepth1;
    public final String labelDepth2;
    public final String labelDepth3;
    public final String parking;
    public final String petCarriage;
    public final String babyCarriage;
    public final LocalTime openTime;
    public final LocalTime closeTime;
    public final String time;
    public final String dayOff;


    public boolean isOpenAt(LocalTime currentTime, String dayOfWeek) {
        if (dayOff != null && dayOff.equals(dayOfWeek)) {
            return false;
        }

        if (openTime != null && closeTime != null) {
            return (!currentTime.isBefore(openTime) && !currentTime.isAfter(closeTime));

        }else{
            throw new SpotRunningTimeNullException(id);
        }
    }

}
