package yunrry.flik.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yunrry.flik.core.domain.model.card.Festival;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DiscriminatorValue("FESTIVAL")
public class FestivalEntity extends BaseSpotEntity {

    @Column(name = "event_start_date", length = 500)
    private String eventStartDate;

    @Column(name = "event_end_date", length = 500)
    private String eventEndDate;

    @Column(name = "age_limit", length = 500)
    private String ageLimit;

    @Column(name = "sponsor", length = 500)
    private String sponsor;

    @Column(name = "running_time", length = 500)
    private String runningTime;

    @Column(name = "fee", length = 500)
    private String fee;


    public FestivalEntity(Long id, String name, String contentTypeId,String category, String description,
                          String address, String regnCd, String sigunguCd, BigDecimal latitude, BigDecimal Longitude, String imageUrls, String info,BigDecimal rating,
                          String googlePlaceId, Integer reviewCount  ,String tag1, String tag2, String tag3, String tags, String parking, String petCarriage, String babyCarriage,
                          LocalTime openTime, LocalTime closeTime, String time, String dayOff, String eventStartDate, String eventEndDate, String ageLimit, String sponsor, String runningTime, String fee) {
        super(id, name, contentTypeId, category, description, address, regnCd, sigunguCd, latitude, Longitude, imageUrls, info, rating, googlePlaceId, reviewCount, tag1, tag2, tag3, tags, parking, petCarriage, babyCarriage, openTime, closeTime, time, dayOff);
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
        this.ageLimit = ageLimit;
        this.sponsor = sponsor;
        this.runningTime = runningTime;
        this.fee = fee;
    }

    public Festival toDomain() {
        return new Festival(
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
                this.eventStartDate,
                this.eventEndDate,
                this.ageLimit,
                this.sponsor,
                this.runningTime,
                this.fee

        );
    }

    public static FestivalEntity fromDomain(Festival festival) {
        return new FestivalEntity(
                festival.getId(),
                festival.getName(),
                festival.getContentTypeId(),
                festival.getCategory(),
                festival.getDescription(),
                festival.getAddress(),
                festival.getRegnCd(),
                festival.getSigunguCd(),
                festival.getLatitude(),
                festival.getLongitude(),
                joinImageUrls(festival.getImageUrls()),// String으로 변환 필요
                festival.getInfo(),
                festival.getRating(),
                festival.getGooglePlaceId(),
                festival.getReviewCount(),
                festival.getTag1(),
                festival.getTag2(),
                festival.getTag3(),
                festival.getTags(),
                festival.getParking(),
                festival.getPetCarriage(),
                festival.getBabyCarriage(),
                festival.getOpenTime(),
                festival.getCloseTime(),
                festival.getTime(),
                festival.getDayOff(),
                festival.getEventStartDate(),
                festival.getEventEndDate(),
                festival.getAgeLimit(),
                festival.getSponsor(),
                festival.getRunningTime(),
                festival.getFee()
        );
    }



}
