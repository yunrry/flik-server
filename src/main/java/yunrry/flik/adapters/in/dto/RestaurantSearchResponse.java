package yunrry.flik.adapters.in.dto;

import org.springframework.data.domain.Slice;
import yunrry.flik.core.domain.model.Restaurant;

import java.util.List;

public record RestaurantSearchResponse(
        List<RestaurantDetailResponse> content,
        PageableInfo pageable,
        boolean hasNext,
        int numberOfElements
) {
    public static RestaurantSearchResponse from(Slice<Restaurant> slice) {
        List<RestaurantDetailResponse> content = slice.getContent().stream()
                .map(RestaurantDetailResponse::from)
                .toList();

        PageableInfo pageableInfo = new PageableInfo(
                slice.getNumber(),
                slice.getSize(),
                slice.getSort().toString()
        );

        return new RestaurantSearchResponse(
                content,
                pageableInfo,
                slice.hasNext(),
                slice.getNumberOfElements()
        );
    }

    public record PageableInfo(
            int pageNumber,
            int pageSize,
            String sort
    ) {}
}