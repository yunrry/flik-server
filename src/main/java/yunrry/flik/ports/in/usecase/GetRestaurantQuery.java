package yunrry.flik.ports.in.usecase;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetRestaurantQuery {
    private final Long restaurantId;

    public Long getRestaurantId() {
        return restaurantId;
    }
}