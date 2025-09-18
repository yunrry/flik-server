package yunrry.flik.adapters.in.dto.spot;

import com.fasterxml.jackson.annotation.JsonFormat;
import yunrry.flik.core.domain.model.card.Shop;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.core.domain.model.card.TourSpot;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;


public record SpotDetailResponse(
        Long id,
        String name,
        String category,
        BigDecimal rating,
        String description,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        @JsonFormat(pattern = "HH:mm")
        String operatingHours,
        String dayOff,
        Boolean isOpen,
        List<String> imageUrls,

        // 타입별 특수 정보 (Optional)
        String products,     // Shop일 때만 값 존재
        String expGuide,     // TourSpot일 때만 값 존재
        String ageLimit      // TourSpot일 때만 값 존재

) {
    public static SpotDetailResponse from(Spot spot) {
        String operatingHours = formatOperatingHours(spot.getOpenTime(), spot.getCloseTime());
        Boolean isOpen;
        try {
            isOpen = spot.isOpenAt(LocalTime.now(), getCurrentDayOfWeek());
        } catch (Exception e) {
            isOpen = null;
        }


        // 타입별 특수 정보 추출
        String products = null;
        String expGuide = null;
        String ageLimit = null;

        if (spot instanceof Shop shop) {
            products = shop.getProducts();
        } else if (spot instanceof TourSpot tourSpot) {
            expGuide = tourSpot.getExpGuide();
            ageLimit = tourSpot.getAgeLimit();
        }

        return new SpotDetailResponse(
                spot.getId(),
                spot.getName(),
                spot.getCategory(),
                spot.getRating(),
                spot.getDescription(),
                spot.getAddress(),
                spot.getLatitude(),
                spot.getLongitude(),
                operatingHours,
                spot.getDayOff(),
                isOpen,
                normalizeImageUrls(spot.getImageUrls()),
                products,
                expGuide,
                ageLimit
        );
    }

    private static String formatOperatingHours(LocalTime openTime, LocalTime closeTime) {
        if (openTime == null || closeTime == null) {
            return null;
        }
        return java.lang.String.format("%s ~ %s", openTime.toString(), closeTime.toString());
    }

    private static String getCurrentDayOfWeek() {
        return java.time.LocalDate.now().getDayOfWeek().getDisplayName(
                java.time.format.TextStyle.SHORT, java.util.Locale.KOREAN);
    }

    private static List<String> normalizeImageUrls(List<String> imageUrls) {
        if (imageUrls == null) return List.of();
        return imageUrls.stream()
                .flatMap(url -> Arrays.stream(url.replace("[", "").replace("]", "").replace("\"", "").split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
