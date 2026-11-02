package edu.najah.stu.service;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.HoursParser;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.WeeklySchedule;
import edu.najah.stu.repository.InMemoryRestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Restaurant service behaviour that depends on the day of the week.
 */
@ExtendWith(MockitoExtension.class)
class RestaurantServiceScheduleTest {

    private static final Location NEAR_CAMPUS = new Location(32.2189, 35.2400);

    @Mock
    private TimeProvider timeProvider;

    private InMemoryRestaurantRepository repository;
    private RestaurantService service;

    private final Restaurant pizzaAlNajah = restaurant("R1", "Pizza Al Najah", "Sa-Th 07:00-22:00", NEAR_CAMPUS);
    private final Restaurant rafedya = restaurant("R2", "Rafedya", "24/7", NEAR_CAMPUS);
    private final Restaurant thursdayNights = restaurant("R3", "Thursday Nights", "Th 18:00-02:00", NEAR_CAMPUS);
    private final Restaurant monoPizza = restaurant("R4", "Mono Pizza", "Mo-Su 09:00-01:00", null);
    private final Restaurant mystery = new Restaurant("R5", "Mystery", WeeklySchedule.unknown(),
            NEAR_CAMPUS, Cuisine.OTHER);

    @BeforeEach
    void setUp() {
        repository = new InMemoryRestaurantRepository();
        service = new RestaurantService(repository, timeProvider);
    }

    private static Restaurant restaurant(String id, String name, String hours, Location location) {
        return new Restaurant(id, name, HoursParser.parse(hours, HoursSource.OSM), location, Cuisine.OTHER);
    }

    private void clockAt(int day, int hour, int minute) {
        when(timeProvider.now()).thenReturn(LocalDateTime.of(2026, 10, day, hour, minute));
    }

    private static List<String> names(List<NearbyRestaurant> results) {
        return results.stream().map(result -> result.restaurant().getName()).toList();
    }

    // W14: Friday noon
    @Test
    void closedOnFridayIsLeftOutOfNearbyResults() {
        repository.save(pizzaAlNajah);
        repository.save(rafedya);
        clockAt(30, 12, 0);

        assertEquals(List.of("Rafedya"), names(service.findNearby(NEAR_CAMPUS, 1)));
    }

    @Test
    void sameRestaurantShowsUpOnSaturday() {
        repository.save(pizzaAlNajah);
        clockAt(31, 12, 0);

        assertEquals(List.of("Pizza Al Najah"), names(service.findNearby(NEAR_CAMPUS, 1)));
    }

    // W9
    @Test
    void allDayRestaurantIsNeverClosingSoon() {
        clockAt(30, 23, 55);

        assertFalse(service.isClosingSoon(rafedya));
    }

    // W10
    @Test
    void unknownHoursAreNeverListedAsOpen() {
        repository.save(mystery);
        repository.save(rafedya);
        clockAt(30, 12, 0);

        assertEquals(List.of(rafedya), service.getOpenRestaurants());
    }

    // W11: Friday 01:45, Thursday's shift ends at 02:00
    @Test
    void warnsAfterMidnightWhenYesterdaysShiftEndsSoon() {
        clockAt(30, 1, 45);

        assertTrue(service.isClosingSoon(thursdayNights));
    }

    @Test
    void openAfterMidnightFromYesterdaysShift() {
        repository.save(thursdayNights);
        clockAt(30, 1, 0);

        assertEquals(List.of(thursdayNights), service.getOpenRestaurants());
    }

    // L1
    @Test
    void restaurantWithoutLocationIsStillListedAsOpen() {
        repository.save(monoPizza);
        clockAt(30, 12, 0);

        assertEquals(List.of(monoPizza), service.getOpenRestaurants());
    }
}
