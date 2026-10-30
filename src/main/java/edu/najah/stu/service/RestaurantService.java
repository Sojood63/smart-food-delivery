package edu.najah.stu.service;

import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.repository.RestaurantRepository;

import java.time.LocalTime;
import java.util.List;

/**
 * Lists the restaurants a customer can order from right now.
 *
 * <p>The current time comes from a {@link TimeProvider}, so tests can fix
 * the clock at any moment of the day.</p>
 */
public class RestaurantService {

    /** A restaurant closing in fewer minutes than this is "closing soon". */
    static final long CLOSING_SOON_MINUTES = 30;

    private final RestaurantRepository restaurantRepository;
    private final TimeProvider timeProvider;

    /**
     * Creates the service.
     *
     * @param restaurantRepository where restaurants are stored; must not be null
     * @param timeProvider         source of the current time; must not be null
     * @throws IllegalArgumentException if an argument is null
     */
    public RestaurantService(RestaurantRepository restaurantRepository, TimeProvider timeProvider) {
        if (restaurantRepository == null || timeProvider == null) {
            throw new IllegalArgumentException("Repository and time provider must not be null");
        }
        this.restaurantRepository = restaurantRepository;
        this.timeProvider = timeProvider;
    }

    /**
     * Returns the restaurants that are open now, in the order they were saved.
     * Closed restaurants are left out.
     *
     * @return the open restaurants; empty if none is open
     */
    public List<Restaurant> getOpenRestaurants() {
        LocalTime now = currentTime();
        return restaurantRepository.findAll().stream()
                .filter(restaurant -> restaurant.isOpenAt(now))
                .toList();
    }

    /**
     * Tells whether an open restaurant closes in less than 30 minutes,
     * so the customer can be warned before ordering.
     *
     * @param restaurant the restaurant to check; must not be null
     * @return {@code true} if it is open and closes in under 30 minutes
     */
    public boolean isClosingSoon(Restaurant restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant must not be null");
        }
        LocalTime now = currentTime();
        return restaurant.isOpenAt(now)
                && restaurant.getOpeningHours().minutesUntilClose(now) < CLOSING_SOON_MINUTES;
    }

    private LocalTime currentTime() {
        return timeProvider.now().toLocalTime();
    }
}
