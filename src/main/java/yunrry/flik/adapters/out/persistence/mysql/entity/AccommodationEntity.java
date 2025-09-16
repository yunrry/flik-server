package yunrry.flik.adapters.out.persistence.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yunrry.flik.core.domain.model.card.Accommodation;

import java.math.BigDecimal;
import java.time.LocalTime;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DiscriminatorValue("ACCOMMODATION")
public class AccommodationEntity extends BaseSpotEntity {

    @Column(name = "cuisine_type", length = 500)
    private String reservation;

    @Column(name = "check_in_time", length = 500)
    private String checkInTime;

    @Column(name = "check_out_time", length = 500)
    private String checkOutTime;

    @Column(name = "cooking")
    private boolean cooking;

    @Column(name = "facilities", length = 500)
    private String facilities;


    public AccommodationEntity(Long id, String name, String contentTypeId, String contentId, String category, String description,
                               String address, String regnCd, String signguCd, BigDecimal latitude, BigDecimal Longitude, String imageUrls, String info, BigDecimal rating,
                               String googlePlaceId, Integer reviewCount  , String tag1, String tag2, String tag3, String tags, String labelDepth1, String labelDepth2, String labelDepth3, String parking, String petCarriage, String babyCarriage,
                               LocalTime openTime, LocalTime closeTime, String time, String dayOff, String googleReviews, String reservation, String checkInTime,
                               String checkOutTime, boolean cooking, String facilities) {
        super(id, name, contentTypeId, contentId, category, description, address, regnCd, signguCd, latitude, Longitude, imageUrls, info, rating, googlePlaceId, reviewCount, tag1, tag2, tag3, tags, labelDepth1, labelDepth2, labelDepth3, parking, petCarriage, babyCarriage, openTime, closeTime, time, dayOff, googleReviews);
        this.reservation = reservation;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.cooking = cooking;
        this.facilities = facilities;
    }

    public Accommodation toDomain() {
        return new Accommodation(
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
                this.toDomain().getLabelDepth1(),
                this.toDomain().getLabelDepth2(),
                this.toDomain().getLabelDepth3(),
                this.getParking(),
                this.getPetCarriage(),
                this.getBabyCarriage(),
                this.getOpenTime(),
                this.getCloseTime(),
                this.getTime(),
                this.getDayOff(),
                this.getGoogleReviews(),
                this.reservation,
                this.checkInTime,
                this.checkOutTime,
                this.cooking,
                this.facilities
        );
    }


    public static AccommodationEntity fromDomain(Accommodation accommodation) {
        return new AccommodationEntity(
                accommodation.getId(),
                accommodation.getName(),
                accommodation.getContentTypeId(),
                accommodation.getContentId(),
                accommodation.getCategory(),
                accommodation.getDescription(),
                accommodation.getAddress(),
                accommodation.getRegnCd(),
                accommodation.getSignguCd(),
                accommodation.getLatitude(),
                accommodation.getLongitude(),
                joinImageUrls(accommodation.getImageUrls()),// String으로 변환 필요
                accommodation.getInfo(),
                accommodation.getRating(),
                accommodation.getGooglePlaceId(),
                accommodation.getReviewCount(),
                accommodation.getTag1(),
                accommodation.getTag2(),
                accommodation.getTag3(),
                accommodation.getTags(),
                accommodation.getLabelDepth1(),
                accommodation.getLabelDepth2(),
                accommodation.getLabelDepth3(),
                accommodation.getParking(),
                accommodation.getPetCarriage(),
                accommodation.getBabyCarriage(),
                accommodation.getOpenTime(),
                accommodation.getCloseTime(),
                accommodation.getTime(),
                accommodation.getDayOff(),
                accommodation.getGoogleReviews(),
                accommodation.getReservation(),
                accommodation.getCheckInTime(),
                accommodation.getCheckOutTime(),
                accommodation.isCooking(),
                accommodation.getFacilities()
        );
    }


}