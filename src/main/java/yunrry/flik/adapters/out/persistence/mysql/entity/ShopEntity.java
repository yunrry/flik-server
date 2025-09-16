package yunrry.flik.adapters.out.persistence.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yunrry.flik.core.domain.model.card.Shop;

import java.math.BigDecimal;
import java.time.LocalTime;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DiscriminatorValue("SHOP")
public class ShopEntity extends BaseSpotEntity {

    @Column(name = "products")
    private String products;


    public ShopEntity(Long id, String name, String contentTypeId, String contentId, String category, String description,
                      String address, String regnCd, String signguCd, BigDecimal latitude, BigDecimal Longitude, String imageUrls, String info,BigDecimal rating,
                      String googlePlaceId, Integer reviewCount, String tag1, String tag2, String tag3, String tags, String labelDepth1, String labelDepth2, String labelDepth3, String parking, String petCarriage, String babyCarriage,
                      LocalTime openTime, LocalTime closeTime, String time, String dayOff, String googleReviews, String products) {
        super(id, name, contentTypeId, contentId,category, description, address, regnCd, signguCd, latitude, Longitude, imageUrls, info, rating, googlePlaceId, reviewCount, tag1, tag2, tag3, tags, labelDepth1, labelDepth2, labelDepth3, parking, petCarriage, babyCarriage, openTime, closeTime, time, dayOff, googleReviews);
        this.products = products;
    }

    public Shop toDomain() {
        return new Shop(
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
                this.products
        );
    }

    public static ShopEntity fromDomain(Shop shop) {
        return new ShopEntity(
                shop.getId(),
                shop.getName(),
                shop.getContentTypeId(),
                shop.getContentId(),
                shop.getCategory(),
                shop.getDescription(),
                shop.getAddress(),
                shop.getRegnCd(),
                shop.getSignguCd(),
                shop.getLatitude(),
                shop.getLongitude(),
                joinImageUrls(shop.getImageUrls()),// String으로 변환 필요
                shop.getInfo(),
                shop.getRating(),
                shop.getGooglePlaceId(),
                shop.getReviewCount(),
                shop.getTag1(),
                shop.getTag2(),
                shop.getTag3(),
                shop.getTags(),
                shop.getLabelDepth1(),
                shop.getLabelDepth2(),
                shop.getLabelDepth3(),
                shop.getParking(),
                shop.getPetCarriage(),
                shop.getBabyCarriage(),
                shop.getOpenTime(),
                shop.getCloseTime(),
                shop.getTime(),
                shop.getDayOff(),
                shop.getGoogleReviews(),
                shop.getProducts()
        );
    }


}