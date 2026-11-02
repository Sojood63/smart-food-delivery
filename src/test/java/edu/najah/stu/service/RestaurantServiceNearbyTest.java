package edu.najah.stu.service;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.OpeningHours;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.repository.InMemoryRestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceNearbyTest {

    /** Kilometres in one degree of latitude, matching the haversine formula. */
    private static final double KM_PER_DEGREE = 6371.0 * Math.PI / 180;

    private static final Location FROM = new Location(32.0, 35.0);
    private static final OpeningHours DAY_HOURS = new OpeningHours(LocalTime.of(9, 0), LocalTime.of(22, 0));

    @Mock
    private TimeProvider timeProvider;

    private InMemoryRestaurantRepository repository;
    private RestaurantService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRestaurantRepository();
        service = new RestaurantService(repository, timeProvider);
    }

    /** A point straight north of {@link #FROM}, so its distance is exact. */
    private static Location kmNorth(double km) {
        return new Location(FROM.getLatitude() + km / KM_PER_DEGREE, FROM.getLongitude());
    }

    private Restaurant save(String id, String name, double kmAway, Cuisine cuisine, OpeningHours hours) {
        Restaurant restaurant = new Restaurant(id, name, hours, kmNorth(kmAway), cuisine);
        repository.save(restaurant);
        return restaurant;
    }

    private Restaurant save(String id, String name, double kmAway, Cuisine cuisine) {
        return save(id, name, kmAway, cuisine, DAY_HOURS);
    }

    private void clockAt(int hour, int minute) {
        when(timeProvider.now()).thenReturn(LocalDateTime.of(2026, 10, 30, hour, minute));
    }

    private static List<String> names(List<NearbyRestaurant> results) {
        return results.stream().map(result -> result.restaurant().getName()).toList();
    }

    // N1
    @Test
    void returnsResultsNearestFirst() {
        save("R1", "Far", 1.5, Cuisine.PIZZA);
        save("R2", "Near", 0.3, Cuisine.PIZZA);
        save("R3", "Middle", 0.9, Cuisine.PIZZA);
        clockAt(12, 0);

        assertEquals(List.of("Near", "Middle", "Far"), names(service.findNearby(FROM, 2)));
    }

    // N2
    @Test
    void leavesOutRestaurantsBeyondTheRadius() {
        save("R1", "Near", 0.5, Cuisine.PIZZA);
        save("R2", "Too far", 3.0, Cuisine.PIZZA);
        clockAt(12, 0);

        assertEquals(List.of("Near"), names(service.findNearby(FROM, 2)));
    }

    // N3
    @Test
    void includesRestaurantExactlyOnTheRadius() {
        Restaurant edge = save("R1", "Edge", 1.0, Cuisine.PIZZA);
        double exactRadius = FROM.distanceKmTo(edge.getLocation());
        clockAt(12, 0);

        assertEquals(List.of("Edge"), names(service.findNearby(FROM, exactRadius)));
    }

    // N4
    @Test
    void leavesOutClosedRestaurantsEvenWhenNear() {
        save("R1", "Day place", 0.2, Cuisine.PIZZA);
        save("R2", "Night place", 0.4, Cuisine.SHAWARMA,
                new OpeningHours(LocalTime.of(18, 0), LocalTime.of(2, 0)));
        clockAt(23, 0);

        assertEquals(List.of("Night place"), names(service.findNearby(FROM, 2)));
    }

    // N5
    @Test
    void filtersByCuisine() {
        save("R1", "Knafeh shop", 0.5, Cuisine.SWEETS);
        save("R2", "Pizza shop", 0.2, Cuisine.PIZZA);
        clockAt(12, 0);

        assertEquals(List.of("Knafeh shop"), names(service.findNearby(FROM, 2, Cuisine.SWEETS)));
    }

    // N6
    @Test
    void returnsEmptyListWhenNothingMatches() {
        save("R1", "Pizza shop", 0.2, Cuisine.PIZZA);
        clockAt(12, 0);

        assertTrue(service.findNearby(FROM, 2, Cuisine.SEAFOOD).isEmpty());
    }

    // N7
    @Test
    void rejectsRadiusThatIsNotPositive() {
        assertThrows(IllegalArgumentException.class, () -> service.findNearby(FROM, 0));
        assertThrows(IllegalArgumentException.class, () -> service.findNearby(FROM, -1));
        assertThrows(IllegalArgumentException.class, () -> service.findNearby(FROM, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> service.findNearby(FROM, Double.POSITIVE_INFINITY));
    }

    // N8
    @Test
    void rejectsMissingSearchPointOrCuisine() {
        assertThrows(IllegalArgumentException.class, () -> service.findNearby(null, 2));
        assertThrows(IllegalArgumentException.class, () -> service.findNearby(FROM, 2, null));
    }

    // N9
    @Test
    void estimatesWalkingTimeAtFiveKmPerHour() {
        save("R1", "One km away", 1.0, Cuisine.PIZZA);
        clockAt(12, 0);

        NearbyRestaurant result = service.findNearby(FROM, 2).get(0);

        assertEquals(1.0, result.distanceKm(), 0.001);
        assertEquals(12, result.walkingMinutes());
    }

    @Test
    void roundsWalkingTimeToNearestMinute() {
        save("R1", "Close", 0.15, Cuisine.PIZZA);
        clockAt(12, 0);

        assertEquals(2, service.findNearby(FROM, 2).get(0).walkingMinutes());
    }

    @Test
    void walkingTimeIsAtLeastOneMinute() {
        save("R1", "Next door", 0.01, Cuisine.PIZZA);
        clockAt(12, 0);

        assertEquals(1, service.findNearby(FROM, 2).get(0).walkingMinutes());
    }

    // N10
    @Test
    void ordersRestaurantsAtTheSameDistanceByName() {
        save("R1", "Zaatar House", 0.5, Cuisine.CAFE);
        save("R2", "Abu Ahmad", 0.5, Cuisine.CAFE);
        clockAt(12, 0);

        assertEquals(List.of("Abu Ahmad", "Zaatar House"), names(service.findNearby(FROM, 2)));
    }

    // N11
    @Test
    void marksRestaurantsClosingSoon() {
        save("R1", "Closing soon", 0.2, Cuisine.PIZZA);
        clockAt(21, 40);

        assertTrue(service.findNearby(FROM, 2).get(0).closingSoon());
    }

    @Test
    void doesNotMarkRestaurantsClosingLater() {
        save("R1", "Open late", 0.2, Cuisine.PIZZA);
        clockAt(12, 0);

        assertFalse(service.findNearby(FROM, 2).get(0).closingSoon());
    }
}
