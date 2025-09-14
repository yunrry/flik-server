package yunrry.flik.core.domain.model.card;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Getter
public class Festival extends Spot {

    private final String eventStartDate;
    private final String eventEndDate;
    private final String ageLimit;
    private final String sponsor;
    private final String runningTime;
    private final String fee;


    public Festival(Long id, String name, String contentTypeId, String contentId, String category, String description,
                    String address, String regnCd, String signguCd, BigDecimal latitude, BigDecimal Longitude, List<String> imageUrls, String info,BigDecimal rating,
                    String googlePlaceId, Integer reviewCount  ,String tag1, String tag2, String tag3, String tags, String labelDepth1, String labelDepth2, String labelDepth3,  String parking, String petCarriage, String babyCarriage,
                    LocalTime openTime, LocalTime closeTime, String time, String dayOff, String eventStartDate, String eventEndDate, String ageLimit, String sponsor, String runningTime, String fee) {
        super(id, name, contentTypeId, contentId, category, description, address, regnCd, signguCd, latitude, Longitude, imageUrls, info, rating, googlePlaceId, reviewCount, tag1, tag2, tag3, tags, labelDepth1, labelDepth2, labelDepth3, parking, petCarriage, babyCarriage, openTime, closeTime, time, dayOff);

        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
        this.ageLimit = ageLimit;
        this.sponsor = sponsor;
        this.runningTime = runningTime;
        this.fee = fee;
    }
}
