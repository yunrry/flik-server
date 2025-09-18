package yunrry.flik.core.domain.model.card;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Getter
public class Shop extends Spot {

    private final String products;

    public Shop(Long id, String name, String contentTypeId, String contentId, String category, String description,
                String address, String regnCd, String signguCd, BigDecimal latitude, BigDecimal Longitude, List<String> imageUrls, String info,BigDecimal rating,
                String googlePlaceId, Integer reviewCount  ,String tag1, String tag2, String tag3, String tags, String labelDepth1, String labelDepth2, String labelDepth3, String parking, String petCarriage, String babyCarriage,
                LocalTime openTime, LocalTime closeTime, String time, String dayOff, String googleReviews, String products) {
        super(id, name, contentTypeId, contentId,category, description, address, regnCd, signguCd, latitude, Longitude, imageUrls, info, rating, googlePlaceId, reviewCount, tag1, tag2, tag3, tags, labelDepth1, labelDepth2, labelDepth3, parking, petCarriage, babyCarriage, openTime, closeTime, time, dayOff, googleReviews);
        this.products = products;
    }

    @Override
    public Shop withTags(String tag1, String tag2, String tag3, String tags) {
        return new Shop(
                this.id, this.name, this.contentTypeId, this.contentId, this.category,
                this.description, this.address, this.regnCd, this.signguCd,
                this.latitude, this.longitude, this.imageUrls, this.info, this.rating,
                this.googlePlaceId, this.reviewCount,
                tag1, tag2, tag3, tags, // 새 태그들
                this.labelDepth1, this.labelDepth2, this.labelDepth3,
                this.parking, this.petCarriage, this.babyCarriage,
                this.openTime, this.closeTime, this.time, this.dayOff, this.googleReviews,
                this.products
        );
    }

}