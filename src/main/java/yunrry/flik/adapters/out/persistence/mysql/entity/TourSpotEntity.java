package yunrry.flik.adapters.out.persistence.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yunrry.flik.core.domain.model.card.TourSpot;

import java.math.BigDecimal;
import java.time.LocalTime;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DiscriminatorValue("TOUR_SPOT")
public class TourSpotEntity extends BaseSpotEntity {

    @Column(name = "exp_guide", columnDefinition = "TEXT")
    private String expGuide;

    @Column(name = "age_limit", length = 500)
    private String ageLimit;


    public TourSpotEntity(Long id, String name, String contentTypeId, String contentId, String category, String description,
                          String address, String regnCd, String signguCd, BigDecimal latitude, BigDecimal Longitude, String imageUrls, String info,BigDecimal rating,
                          String googlePlaceId, Integer reviewCount  ,String tag1, String tag2, String tag3, String tags, String labelDepth1, String labelDepth2, String labelDepth3, String parking, String petCarriage, String babyCarriage,
                          LocalTime openTime, LocalTime closeTime, String time, String dayOff, String expGuide, String ageLimit) {
        super(id, name, contentTypeId, contentId, category, description, address, regnCd, signguCd, latitude, Longitude, imageUrls, info, rating, googlePlaceId, reviewCount, tag1, tag2, tag3, tags, labelDepth1, labelDepth2, labelDepth3, parking, petCarriage, babyCarriage, openTime, closeTime, time, dayOff);
        this.expGuide = expGuide;
        this.ageLimit = ageLimit;
    }

    public TourSpot toDomain() {
        return new TourSpot(
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
                this.expGuide,
                this.ageLimit
        );
    }

    public static TourSpotEntity fromDomain(TourSpot tourSpot) {
        return new TourSpotEntity(
                tourSpot.getId(),
                tourSpot.getName(),
                tourSpot.getContentTypeId(),
                tourSpot.getContentId(),
                tourSpot.getCategory(),
                tourSpot.getDescription(),
                tourSpot.getAddress(),
                tourSpot.getRegnCd(),
                tourSpot.getSignguCd(),
                tourSpot.getLatitude(),
                tourSpot.getLongitude(),
                joinImageUrls(tourSpot.getImageUrls()),// String으로 변환 필요
                tourSpot.getInfo(),
                tourSpot.getRating(),
                tourSpot.getGooglePlaceId(),
                tourSpot.getReviewCount(),
                tourSpot.getTag1(),
                tourSpot.getTag2(),
                tourSpot.getTag3(),
                tourSpot.getTags(),
                tourSpot.getLabelDepth1(),
                tourSpot.getLabelDepth2(),
                tourSpot.getLabelDepth3(),
                tourSpot.getParking(),
                tourSpot.getPetCarriage(),
                tourSpot.getBabyCarriage(),
                tourSpot.getOpenTime(),
                tourSpot.getCloseTime(),
                tourSpot.getTime(),
                tourSpot.getDayOff(),
                tourSpot.getExpGuide(),
                tourSpot.getAgeLimit()
        );
    }


}