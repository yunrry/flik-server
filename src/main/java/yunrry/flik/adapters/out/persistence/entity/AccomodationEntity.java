package yunrry.flik.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yunrry.flik.core.domain.model.card.Accomodation;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DiscriminatorValue("ACCOMMODATION")
public class AccomodationEntity extends BaseSpotEntity {

    @Column(name = "cuisine_type", length = 500)
    private String reservation;

    @Column(name = "check_in_time", length = 500)
    private String checkInTime;

    @Column(name = "check_out_time", length = 500)
    private String checkOutTime;

    @Column(name = "cooking", length = 500)
    private boolean cooking;

    @Column(name = "facilities", length = 500)
    private String facilities;


    public AccomodationEntity(Long id, String name, String contentTypeId,String category, String description,
                              String address, String regnCd, String sigunguCd, BigDecimal latitude, BigDecimal Longitude, String imageUrls, String info,BigDecimal rating,
                              String googlePlaceId, Integer reviewCount  ,String tag1, String tag2, String tag3, String tags, String parking, String petCarriage, String babyCarriage,
                              LocalTime openTime, LocalTime closeTime, String time, String dayOff, String reservation, String checkInTime,
                              String checkOutTime, boolean cooking, String facilities) {
        super(id, name, contentTypeId, category, description, address, regnCd, sigunguCd, latitude, Longitude, imageUrls, info, rating, googlePlaceId, reviewCount, tag1, tag2, tag3, tags, parking, petCarriage, babyCarriage, openTime, closeTime, time, dayOff);
        this.reservation = reservation;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.cooking = cooking;
        this.facilities = facilities;
    }

    public Accomodation toDomain() {
        return new Accomodation(
                this.getId(),
                this.getName(),
                this.getContentTypeId(),
                this.getCategory(),
                this.getDescription(),
                this.getAddress(),
                this.getRegnCd(),
                this.getSigunguCd(),
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
                this.getParking(),
                this.getPetCarriage(),
                this.getBabyCarriage(),
                this.getOpenTime(),
                this.getCloseTime(),
                this.getTime(),
                this.getDayOff(),
                this.reservation,
                this.checkInTime,
                this.checkOutTime,
                this.cooking,
                this.facilities
        );
    }

    public static AccomodationEntity fromDomain(Accomodation accomodation) {
        return new AccomodationEntity(
                accomodation.getId(),
                accomodation.getName(),
                accomodation.getContentTypeId(),
                accomodation.getCategory(),
                accomodation.getDescription(),
                accomodation.getAddress(),
                accomodation.getRegnCd(),
                accomodation.getSigunguCd(),
                accomodation.getLatitude(),
                accomodation.getLongitude(),
                joinImageUrls(accomodation.getImageUrls()),// String으로 변환 필요
                accomodation.getInfo(),
                accomodation.getRating(),
                accomodation.getGooglePlaceId(),
                accomodation.getReviewCount(),
                accomodation.getTag1(),
                accomodation.getTag2(),
                accomodation.getTag3(),
                accomodation.getTags(),
                accomodation.getParking(),
                accomodation.getPetCarriage(),
                accomodation.getBabyCarriage(),
                accomodation.getOpenTime(),
                accomodation.getCloseTime(),
                accomodation.getTime(),
                accomodation.getDayOff(),
                accomodation.getReservation(),
                accomodation.getCheckInTime(),
                accomodation.getCheckOutTime(),
                accomodation.isCooking(),
                accomodation.getFacilities()
        );
    }


}