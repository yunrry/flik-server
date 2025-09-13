package yunrry.flik.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yunrry.flik.core.domain.model.card.Leisure;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DiscriminatorValue("LEISURE")
public class LeisureEntity extends BaseSpotEntity {


    @Column(name = "fee")
    private String fee;


    public LeisureEntity(Long id, String name, String contentTypeId, String contentId, String category, String description,
                         String address, String regnCd, String signguCd, BigDecimal latitude, BigDecimal Longitude, String imageUrls, String info,BigDecimal rating,
                         String googlePlaceId, Integer reviewCount  ,String tag1, String tag2, String tag3, String tags, String labelDepth1, String labelDepth2, String labelDepth3,  String parking, String petCarriage, String babyCarriage,
                         LocalTime openTime, LocalTime closeTime, String time, String dayOff, String fee) {
        super(id, name, contentTypeId, contentId, category, description, address, regnCd, signguCd, latitude, Longitude, imageUrls, info, rating, googlePlaceId, reviewCount, tag1, tag2, tag3, tags, labelDepth1, labelDepth2, labelDepth3, parking, petCarriage, babyCarriage, openTime, closeTime, time, dayOff);
        this.fee = fee;
    }

    public Leisure toDomain() {
        return new Leisure(
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
                this.fee

        );
    }

    public static LeisureEntity fromDomain(Leisure leisure) {
        return new LeisureEntity(
                leisure.getId(),
                leisure.getName(),
                leisure.getContentTypeId(),
                leisure.getContentId(),
                leisure.getCategory(),
                leisure.getDescription(),
                leisure.getAddress(),
                leisure.getRegnCd(),
                leisure.getSignguCd(),
                leisure.getLatitude(),
                leisure.getLongitude(),
                joinImageUrls(leisure.getImageUrls()),// String으로 변환 필요
                leisure.getInfo(),
                leisure.getRating(),
                leisure.getGooglePlaceId(),
                leisure.getReviewCount(),
                leisure.getTag1(),
                leisure.getTag2(),
                leisure.getTag3(),
                leisure.getTags(),
                leisure.getLabelDepth1(),
                leisure.getLabelDepth2(),
                leisure.getLabelDepth3(),
                leisure.getParking(),
                leisure.getPetCarriage(),
                leisure.getBabyCarriage(),
                leisure.getOpenTime(),
                leisure.getCloseTime(),
                leisure.getTime(),
                leisure.getDayOff(),
                leisure.getFee()
        );
    }


}
