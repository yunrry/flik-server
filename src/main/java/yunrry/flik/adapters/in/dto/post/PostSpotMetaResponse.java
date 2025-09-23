package yunrry.flik.adapters.in.dto.post;

import yunrry.flik.core.domain.model.card.Spot;


public record PostSpotMetaResponse(
        Long spotId,
        String name,
        String regionCode,
        String imageUrl,
        String category
) {
    public static PostSpotMetaResponse from(Spot spot) {
        if (spot == null) return null;

        return new PostSpotMetaResponse(
                spot.getId(),
                spot.getName(),
                spot.getRegnCd() + spot.getSignguCd(),
                spot.getImageUrls().isEmpty() ? null : spot.getImageUrls().get(0),
                spot.getCategory()
        );
    }


}