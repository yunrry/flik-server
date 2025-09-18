package yunrry.flik.adapters.out.persistence.mysql.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yunrry.flik.core.domain.model.card.Restaurant;

import java.math.BigDecimal;
import java.time.LocalTime;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DiscriminatorValue("RESTAURANT")
public class RestaurantEntity extends BaseSpotEntity {

    @Column(name = "cuisine_type", length = 500)
    private String cuisineType;

    @Column(name = "price_range", length = 500)
    private String priceRange;

    @Column(name = "reservation", length = 500)
    private String reservation;

    @Column(name = "kids_facility", length = 500)
    private String kidsFacility;

    @Column(name = "take_away", length = 500)
    private String takeAway;

    @Column(name = "first_menu", length = 500)
    private String firstMenu;

    @Column(name = "treat_menu", columnDefinition = "TEXT")
    private String treatMenu;


    public RestaurantEntity(Long id, String name, String contentTypeId, String contentId, String category, String description,
                            String address, String regnCd, String signguCd, BigDecimal latitude, BigDecimal Longitude, String imageUrls, String info,BigDecimal rating,
                            String googlePlaceId, Integer reviewCount  ,String tag1, String tag2, String tag3, String tags, String labelDepth1, String labelDepth2, String labelDepth3, String parking, String petCarriage, String babyCarriage,
                            LocalTime openTime, LocalTime closeTime, String time, String dayOff, String googleReviews, String cuisineType, String priceRange, String reservation, String kidsFacility, String takeAway, String firstMenu, String treatMenu) {
        super(id, name, contentTypeId, contentId, category, description, address, regnCd, signguCd, latitude, Longitude, imageUrls, info, rating, googlePlaceId, reviewCount, tag1, tag2, tag3, tags, labelDepth1,labelDepth2, labelDepth3, parking, petCarriage, babyCarriage, openTime, closeTime, time, dayOff, googleReviews);
        this.cuisineType = cuisineType;
        this.priceRange = priceRange;
        this.reservation = reservation;
        this.kidsFacility = kidsFacility;
        this.takeAway = takeAway;
        this.firstMenu = firstMenu;
        this.treatMenu = treatMenu;
    }

    public Restaurant toDomain() {
        return new Restaurant(
                this.getId(),
                this.getName(),
                this.getContentTypeId(),
                this.getContentId(),
                this.getCategory(),
                this.getDescription(),
                this.getAddress(),
                this.getRegnCd(),
                this.getSignguCd(),
                this.getLatitude(),
                this.getLongitude(),
                parseImageUrls(this.getImageUrls()),
                this.getInfo(),
                this.getRating(),
                this.getGooglePlaceId(),
                this.getReviewCount(),
                this.getTag1(),
                this.getTag2(),
                this.getTag3(),
                this.getTags(),
                this.getLabelDepth1(),
                this.getLabelDepth2(),
                this.getLabelDepth3(),
                this.getParking(),
                this.getPetCarriage(),
                this.getBabyCarriage(),
                this.getOpenTime(),
                this.getCloseTime(),
                this.getTime(),
                this.getDayOff(),
                this.getGoogleReviews(),
                this.cuisineType,
                this.priceRange,
                this.reservation,
                this.kidsFacility,
                this.takeAway,
                this.firstMenu,
                this.treatMenu
        );
    }

    public static RestaurantEntity fromDomain(Restaurant restaurant) {
        return new RestaurantEntity(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getContentTypeId(),
                restaurant.getContentId(),
                restaurant.getCategory(),
                restaurant.getDescription(),
                restaurant.getAddress(),
                restaurant.getRegnCd(),
                restaurant.getSignguCd(),
                restaurant.getLatitude(),
                restaurant.getLongitude(),
                joinImageUrls(restaurant.getImageUrls()),// String으로 변환 필요
                restaurant.getInfo(),
                restaurant.getRating(),
                restaurant.getGooglePlaceId(),
                restaurant.getReviewCount(),
                restaurant.getTag1(),
                restaurant.getTag2(),
                restaurant.getTag3(),
                restaurant.getTags(),
                restaurant.getLabelDepth1(),
                restaurant.getLabelDepth2(),
                restaurant.getLabelDepth3(),
                restaurant.getParking(),
                restaurant.getPetCarriage(),
                restaurant.getBabyCarriage(),
                restaurant.getOpenTime(),
                restaurant.getCloseTime(),
                restaurant.getTime(),
                restaurant.getDayOff(),
                restaurant.getGoogleReviews(),
                restaurant.getCuisineType(),
                restaurant.getPriceRange(),
                restaurant.getReservation(),
                restaurant.getKidsFacility(),
                restaurant.getTakeAway(),
                restaurant.getFirstMenu(),
                restaurant.getTreatMenu()
        );
    }

}