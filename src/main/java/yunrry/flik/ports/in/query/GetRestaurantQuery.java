package yunrry.flik.ports.in.query;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetRestaurantQuery {
    private final Long restaurantId;

    public Long getRestaurantId() {
        return restaurantId;
    }
}