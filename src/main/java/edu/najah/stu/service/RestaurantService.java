package edu.najah.stu.service;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.repository.RestaurantRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Lists the restaurants a customer can order from right now, and finds
 * the open ones near a given place.
 *
 * <p>The current date and time come from a {@link TimeProvider}, so tests can
 * fix the clock at any moment of any day of the week.</p>
 */
public class RestaurantService {

    /** A restaurant closing in fewer minutes than this is "closing soon". */
    static final long CLOSING_SOON_MINUTES = 30;

    /** Average walking speed used to estimate walking time. */
    static final double WALKING_SPEED_KMH = 5.0;

    private static final double MINUTES_PER_HOUR = 60.0;

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
        LocalDateTime now = currentTime();
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
        return closesSoon(restaurant, currentTime());
    }

    /**
     * Finds the open restaurants within a radius of a place, nearest first.
     * Restaurants at the same distance are ordered by name, and restaurants
     * without a known location are left out.
     *
     * @param from     the place to search from; must not be null
     * @param radiusKm the search radius in kilometres; must be greater than zero
     * @return the open restaurants within the radius; empty if there are none
     * @throws IllegalArgumentException if {@code from} is null or the radius is not positive
     */
    public List<NearbyRestaurant> findNearby(Location from, double radiusKm) {
        return search(from, radiusKm, null);
    }

    /**
     * Finds the open restaurants of one cuisine within a radius of a place, nearest first.
     *
     * @param from     the place to search from; must not be null
     * @param radiusKm the search radius in kilometres; must be greater than zero
     * @param cuisine  the only cuisine to include; must not be null
     * @return the matching open restaurants within the radius; empty if there are none
     * @throws IllegalArgumentException if an argument is null or the radius is not positive
     */
    public List<NearbyRestaurant> findNearby(Location from, double radiusKm, Cuisine cuisine) {
        if (cuisine == null) {
            throw new IllegalArgumentException("Cuisine must not be null");
        }
        return search(from, radiusKm, cuisine);
    }

    private List<NearbyRestaurant> search(Location from, double radiusKm, Cuisine cuisine) {
        if (from == null) {
            throw new IllegalArgumentException("Search location must not be null");
        }
        if (!(radiusKm > 0) || Double.isInfinite(radiusKm)) {
            throw new IllegalArgumentException("Radius must be a positive number: " + radiusKm);
        }
        LocalDateTime now = currentTime();
        return restaurantRepository.findAll().stream()
                .filter(restaurant -> cuisine == null || restaurant.getCuisine() == cuisine)
                .filter(restaurant -> restaurant.isOpenAt(now))
                .flatMap(restaurant -> restaurant.getLocation()
                        .map(location -> toResult(restaurant, from.distanceKmTo(location), now))
                        .stream())
                .filter(result -> result.distanceKm() <= radiusKm)
                .sorted(Comparator.comparingDouble(NearbyRestaurant::distanceKm)
                        .thenComparing(result -> result.restaurant().getName()))
                .toList();
    }

    private NearbyRestaurant toResult(Restaurant restaurant, double distanceKm, LocalDateTime now) {
        long walkingMinutes = Math.max(1, Math.round(distanceKm / WALKING_SPEED_KMH * MINUTES_PER_HOUR));
        return new NearbyRestaurant(restaurant, distanceKm, walkingMinutes, closesSoon(restaurant, now));
    }

    private boolean closesSoon(Restaurant restaurant, LocalDateTime now) {
        LocalDateTime warningLimit = now.plusMinutes(CLOSING_SOON_MINUTES);
        return restaurant.getSchedule().closingTime(now)
                .map(warningLimit::isAfter)
                .orElse(false);
    }

    private LocalDateTime currentTime() {
        return timeProvider.now();
    }
}
