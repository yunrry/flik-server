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


    public LeisureEntity(Long id, String name, String contentTypeId,String category, String description,
                         String address, String regnCd, String sigunguCd, BigDecimal latitude, BigDecimal Longitude, String imageUrls, String info,BigDecimal rating,
                         String googlePlaceId, Integer reviewCount  ,String tag1, String tag2, String tag3, String tags, String parking, String petCarriage, String babyCarriage,
                         LocalTime openTime, LocalTime closeTime, String time, String dayOff, String fee) {
        super(id, name, contentTypeId, category, description, address, regnCd, sigunguCd, latitude, Longitude, imageUrls, info, rating, googlePlaceId, reviewCount, tag1, tag2, tag3, tags, parking, petCarriage, babyCarriage, openTime, closeTime, time, dayOff);
        this.fee = fee;
    }

    public Leisure toDomain() {
        return new Leisure(
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
                this.fee

        );
    }

    public static LeisureEntity fromDomain(Leisure leisure) {
        return new LeisureEntity(
                leisure.getId(),
                leisure.getName(),
                leisure.getContentTypeId(),
                leisure.getCategory(),
                leisure.getDescription(),
                leisure.getAddress(),
                leisure.getRegnCd(),
                leisure.getSigunguCd(),
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
