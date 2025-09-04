package yunrry.flik.adapters.in.dto.festival;

import com.fasterxml.jackson.annotation.JsonFormat;
import yunrry.flik.core.domain.model.Festival;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;


public record FestivalDetailResponse(
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
    public static FestivalDetailResponse from(Festival festival) {
        String operatingHours = formatOperatingHours(festival.getOpenTime(), festival.getCloseTime());
        Boolean isOpen;
        try {
            isOpen = festival.isOpenAt(LocalTime.now(), getCurrentDayOfWeek());
        } catch (Exception e) {
            isOpen = null;
        }

        return new FestivalDetailResponse(
                festival.getId(),
                festival.getName(),
                festival.getCategory(),
                festival.getRating(),
                festival.getDescription(),
                festival.getAddress(),
                festival.getDistance(),
                operatingHours,
                festival.getDayOff(),
                isOpen,
                festival.getImageUrls()
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
