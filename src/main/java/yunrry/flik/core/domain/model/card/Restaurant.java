package yunrry.flik.core.domain.model.card;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Getter
public class Restaurant extends Spot {
    private final String cuisineType;
    private final String priceRange;
    private final String reservation;
    private final String kidsFacility;
    private final String takeAway;
    private final String firstMenu;
    private final String treatMenu;


    public Restaurant(Long id, String name, String contentTypeId,String category, String description,
                      String address, String regnCd, String sigunguCd, BigDecimal latitude, BigDecimal Longitude, List<String> imageUrls, String info,BigDecimal rating,
                      String googlePlaceId, Integer reviewCount  ,String tag1, String tag2, String tag3, String tags, String parking, String petCarriage, String babyCarriage,
                      LocalTime openTime, LocalTime closeTime, String time, String dayOff, String cuisineType, String priceRange, String reservation, String kidsFacility, String takeAway, String firstMenu, String treatMenu) {
        super(id, name, contentTypeId, category, description, address, regnCd, sigunguCd, latitude, Longitude, imageUrls, info, rating, googlePlaceId, reviewCount, tag1, tag2, tag3, tags, parking, petCarriage, babyCarriage, openTime, closeTime, time, dayOff);
        this.cuisineType = cuisineType;
        this.priceRange = priceRange;
        this.reservation = reservation;
        this.kidsFacility = kidsFacility;
        this.takeAway = takeAway;
        this.firstMenu = firstMenu;
        this.treatMenu = treatMenu;
    }

}