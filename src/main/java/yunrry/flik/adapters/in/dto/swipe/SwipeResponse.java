package yunrry.flik.adapters.in.dto.swipe;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스와이프 응답")
public record SwipeResponse(
        @Schema(description = "저장 성공 여부", example = "true")
        boolean saved,

        @Schema(description = "응답 메시지", example = "장소가 저장되었습니다.")
        String message,

        @Schema(description = "장소 ID", example = "12345")
        Long spotId
) {

    public static SwipeResponse success(Long spotId) {
        return new SwipeResponse(true, "장소가 저장되었습니다.", spotId);
    }

    public static SwipeResponse alreadySaved(Long spotId) {
        return new SwipeResponse(false, "이미 저장된 장소입니다.", spotId);
    }

    public static SwipeResponse error(Long spotId, String message) {
        return new SwipeResponse(false, message, spotId);
    }
}