package yunrry.flik.adapters.in.dto.spot;

import com.fasterxml.jackson.annotation.JsonFormat;
import yunrry.flik.core.domain.model.Spot;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;


public record SpotDetailResponse(
        Long id,
        String name,
        String category,
        BigDecimal rating,
        String description,
        String address,
        Integer distance,
        @JsonFormat(pattern = "HH:mm")
        String operatingHours,
        String dayOff,
        Boolean isOpen,
        List<String> imageUrls
) {
    public static SpotDetailResponse from(Spot spot) {
        String operatingHours = formatOperatingHours(spot.getOpenTime(), spot.getCloseTime());
        Boolean isOpen;
        try {
            isOpen = spot.isOpenAt(LocalTime.now(), getCurrentDayOfWeek());
        } catch (Exception e) {
            isOpen = null;
        }

        return new SpotDetailResponse(
                spot.getId(),
                spot.getName(),
                spot.getCategory(),
                spot.getRating(),
                spot.getDescription(),
                spot.getAddress(),
                spot.getDistance(),
                operatingHours,
                spot.getDayOff(),
                isOpen,
                spot.getImageUrls()
        );
    }

    private static String formatOperatingHours(LocalTime openTime, LocalTime closeTime) {
        if (openTime == null || closeTime == null) {
            return null;
        }
        return String.format("%s ~ %s", openTime.toString(), closeTime.toString());
    }

    private static String getCurrentDayOfWeek() {
        return java.time.LocalDate.now().getDayOfWeek().getDisplayName(
                java.time.format.TextStyle.SHORT, java.util.Locale.KOREAN);
    }
}
