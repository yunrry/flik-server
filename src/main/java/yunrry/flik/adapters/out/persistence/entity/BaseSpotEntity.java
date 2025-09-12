package yunrry.flik.adapters.out.persistence.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import yunrry.flik.core.domain.model.card.Spot;


import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;


@Entity
@Table(name = "spots")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "spot_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@NoArgsConstructor
public abstract class BaseSpotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(nullable = false, length = 500)
    private String contentTypeId;

    @Column(nullable = false, length = 500)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(name = "regn_cd", length = 500)
    private String regnCd;

    @Column(name = "signgu_cd", length = 500)
    private String sigunguCd;

    @Column(name = "latitude")
    private BigDecimal latitude;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    @Column(name = "info", length = 500)
    private String info;

    @Column(precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(name = "google_place_id", length = 500)
    private String googlePlaceId;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "tag1", length = 500)
    private String tag1;

    @Column(name = "tag2", length = 500)
    private String tag2;

    @Column(name = "tag3", length = 500)
    private String tag3;

    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "parking", length = 500)
    private String parking;

    @Column(name = "pet_carriage", length = 500)
    private String petCarriage;

    @Column(name = "baby_carriage", length = 500)
    private String babyCarriage;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "time", length = 500)
    private String time;

    @Column(name = "day_off", length = 500)
    private String dayOff;

    protected BaseSpotEntity(Long id, String name, String contentTypeId,String category, String description,
                             String address, String regnCd, String sigunguCd, BigDecimal latitude, BigDecimal Longitude,String imageUrls, String info,BigDecimal rating,
                             String googlePlaceId, Integer reviewCount  ,String tag1, String tag2, String tag3, String tags, String parking, String petCarriage, String babyCarriage,
                              LocalTime openTime, LocalTime closeTime, String time, String dayOff) {
        this.id = id;
        this.name = name;
        this.contentTypeId = contentTypeId;
        this.category = category;
        this.description = description;
        this.address = address;
        this.regnCd = regnCd;
        this.sigunguCd = sigunguCd;
        this.latitude = latitude;
        this.longitude = Longitude;
        this.imageUrls = imageUrls;
        this.info = info;
        this.rating = rating;
        this.googlePlaceId = googlePlaceId;
        this.reviewCount = reviewCount;
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.tag3 = tag3;
        this.tags = tags;
        this.parking = parking;
        this.petCarriage = petCarriage;
        this.babyCarriage = babyCarriage;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.time = time;
        this.dayOff = dayOff;
    }

    public abstract Spot toDomain();

    // 공통 헬퍼 메서드
    protected List<String> parseImageUrls(String imageUrls) {
        if (imageUrls == null || imageUrls.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.asList(imageUrls.split(","));
    }

    protected static String joinImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return null;
        }
        return String.join(",", imageUrls);
    }

}