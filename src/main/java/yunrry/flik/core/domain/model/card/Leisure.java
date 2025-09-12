package yunrry.flik.core.domain.model.card;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Getter
public class Leisure extends Spot {

    private final String fee;

    public Leisure(Long id, String name, String contentTypeId,String category, String description,
                   String address, String regnCd, String sigunguCd, BigDecimal latitude, BigDecimal Longitude, List<String> imageUrls, String info,BigDecimal rating,
                   String googlePlaceId, Integer reviewCount  ,String tag1, String tag2, String tag3, String tags, String parking, String petCarriage, String babyCarriage,
                   LocalTime openTime, LocalTime closeTime, String time, String dayOff, String fee) {
        super(id, name, contentTypeId, category, description, address, regnCd, sigunguCd, latitude, Longitude, imageUrls, info, rating, googlePlaceId, reviewCount, tag1, tag2, tag3, tags, parking, petCarriage, babyCarriage, openTime, closeTime, time, dayOff);

        this.fee = fee;
    }
}
