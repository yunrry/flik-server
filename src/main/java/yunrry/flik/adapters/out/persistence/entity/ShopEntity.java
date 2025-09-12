package yunrry.flik.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yunrry.flik.core.domain.model.card.Shop;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DiscriminatorValue("SHOP")
public class ShopEntity extends BaseSpotEntity {

    @Column(name = "products", columnDefinition = "TEXT")
    private String products;


    public ShopEntity(Long id, String name, String contentTypeId,String category, String description,
                      String address, String regnCd, String sigunguCd, BigDecimal latitude, BigDecimal Longitude, String imageUrls, String info,BigDecimal rating,
                      String googlePlaceId, Integer reviewCount, String tag1, String tag2, String tag3, String tags, String parking, String petCarriage, String babyCarriage,
                      LocalTime openTime, LocalTime closeTime, String time, String dayOff, String products) {
        super(id, name, contentTypeId, category, description, address, regnCd, sigunguCd, latitude, Longitude, imageUrls, info, rating, googlePlaceId, reviewCount, tag1, tag2, tag3, tags, parking, petCarriage, babyCarriage, openTime, closeTime, time, dayOff);
        this.products = products;
    }

    public Shop toDomain() {
        return new Shop(
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
                this.products
        );
    }

    public static ShopEntity fromDomain(Shop shop) {
        return new ShopEntity(
                shop.getId(),
                shop.getName(),
                shop.getContentTypeId(),
                shop.getCategory(),
                shop.getDescription(),
                shop.getAddress(),
                shop.getRegnCd(),
                shop.getSigunguCd(),
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
                shop.getParking(),
                shop.getPetCarriage(),
                shop.getBabyCarriage(),
                shop.getOpenTime(),
                shop.getCloseTime(),
                shop.getTime(),
                shop.getDayOff(),
                shop.getProducts()
        );
    }


}