package yunrry.flik.adapters.out.persistence.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yunrry.flik.core.domain.model.card.Cultural;
import yunrry.flik.core.domain.model.card.Spot;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DiscriminatorValue("CULTURAL")
public class CulturalEntity extends BaseSpotEntity {

    @Column(name = "fee")
    private String fee;


    public CulturalEntity(Long id, String name, String contentTypeId, String contentId, String category, String description,
                          String address, String regnCd, String signguCd, BigDecimal latitude, BigDecimal Longitude, String imageUrls, String info,BigDecimal rating,
                          String googlePlaceId, Integer reviewCount  ,String tag1, String tag2, String tag3, String tags, String labelDepth1, String labelDepth2, String labelDepth3, String parking, String petCarriage, String babyCarriage,
                          LocalTime openTime, LocalTime closeTime, String time, String dayOff, String googleReviews, String fee) {
        super(id, name, contentTypeId, contentId, category, description, address, regnCd, signguCd, latitude, Longitude, imageUrls, info, rating, googlePlaceId, reviewCount, tag1, tag2, tag3, tags, labelDepth1, labelDepth2, labelDepth3, parking, petCarriage, babyCarriage, openTime, closeTime,time, dayOff, googleReviews);
        this.fee = fee;
    }


    public Cultural toDomain() {
        return new Cultural(
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
                this.fee
        );
    }


    public static CulturalEntity fromDomain(Cultural cultural) {
        return new CulturalEntity(
                cultural.getId(),
                cultural.getName(),
                cultural.getContentTypeId(),
                cultural.getContentId(),
                cultural.getCategory(),
                cultural.getDescription(),
                cultural.getAddress(),
                cultural.getRegnCd(),
                cultural.getSignguCd(),
                cultural.getLatitude(),
                cultural.getLongitude(),
                joinImageUrls(cultural.getImageUrls()),// String으로 변환 필요
                cultural.getInfo(),
                cultural.getRating(),
                cultural.getGooglePlaceId(),
                cultural.getReviewCount(),
                cultural.getTag1(),
                cultural.getTag2(),
                cultural.getTag3(),
                cultural.getTags(),
                cultural.getLabelDepth1(),
                cultural.getLabelDepth2(),
                cultural.getLabelDepth3(),
                cultural.getParking(),
                cultural.getPetCarriage(),
                cultural.getBabyCarriage(),
                cultural.getOpenTime(),
                cultural.getCloseTime(),
                cultural.getTime(),
                cultural.getDayOff(),
                cultural.getGoogleReviews(),
                cultural.getFee()
        );
    }


}
