package yunrry.flik.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yunrry.flik.core.domain.model.Festival;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "festivals")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String address;

    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    @Column(precision = 2, scale = 1)
    private BigDecimal rating;

    private Integer distance;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "day_off")
    private String dayOff;

    public Festival toDomain() {
        return Festival.builder()
                .id(this.id)
                .name(this.name)
                .category(this.category)
                .description(this.description)
                .address(this.address)
                .imageUrls(parseImageUrls(this.imageUrls))
                .rating(this.rating)
                .distance(this.distance)
                .openTime(this.openTime)
                .closeTime(this.closeTime)
                .dayOff(this.dayOff)
                .build();
    }

    public static FestivalEntity fromDomain(Festival festival) {
        return builder()
                .id(festival.getId())
                .name(festival.getName())
                .category(festival.getCategory())
                .description(festival.getDescription())
                .address(festival.getAddress())
                .imageUrls(joinImageUrls(festival.getImageUrls()))
                .rating(festival.getRating())
                .distance(festival.getDistance())
                .openTime(festival.getOpenTime())
                .closeTime(festival.getCloseTime())
                .dayOff(festival.getDayOff())
                .build();

    }
    private List<String> parseImageUrls(String imageUrls) {
        if (imageUrls == null || imageUrls.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.asList(imageUrls.split(","));
    }

    private static String joinImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return null;
        }
        return String.join(",", imageUrls);
    }
}
