package edu.najah.stu.service;

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
class RestaurantServiceTest {

    @Mock
    private TimeProvider timeProvider;

    private InMemoryRestaurantRepository repository;
    private RestaurantService service;

    private final Restaurant dayRestaurant = restaurant("R1", "Day Grill", "09:00", "22:00");
    private final Restaurant nightRestaurant = restaurant("R2", "Night Shawarma", "18:00", "02:00");

    @BeforeEach
    void setUp() {
        repository = new InMemoryRestaurantRepository();
        service = new RestaurantService(repository, timeProvider);
    }

    private static Restaurant restaurant(String id, String name, String opensAt, String closesAt) {
        OpeningHours hours = new OpeningHours(LocalTime.parse(opensAt), LocalTime.parse(closesAt));
        return new Restaurant(id, name, hours);
    }

    private void clockAt(String time) {
        when(timeProvider.now()).thenReturn(LocalDateTime.of(2026, 10, 30, 0, 0).with(LocalTime.parse(time)));
    }

    // R1 + R9: open restaurants are shown, closed ones are hidden
    @Test
    void showsOnlyOpenRestaurantsAtNoon() {
        repository.save(dayRestaurant);
        repository.save(nightRestaurant);
        clockAt("12:00");

        assertEquals(List.of(dayRestaurant), service.getOpenRestaurants());
    }

    // R2 + R5: late at night only the overnight restaurant is open
    @Test
    void showsOvernightRestaurantAfterMidnight() {
        repository.save(dayRestaurant);
        repository.save(nightRestaurant);
        clockAt("01:00");

        assertEquals(List.of(nightRestaurant), service.getOpenRestaurants());
    }

    @Test
    void showsBothWhenBothAreOpen() {
        repository.save(dayRestaurant);
        repository.save(nightRestaurant);
        clockAt("20:00");

        assertEquals(List.of(dayRestaurant, nightRestaurant), service.getOpenRestaurants());
    }

    // R10: every restaurant closed
    @Test
    void returnsEmptyListWhenAllAreClosed() {
        repository.save(dayRestaurant);
        repository.save(nightRestaurant);
        clockAt("05:00");

        assertTrue(service.getOpenRestaurants().isEmpty());
    }

    // R11: no restaurants at all
    @Test
    void returnsEmptyListWhenNoRestaurantsExist() {
        clockAt("12:00");

        assertTrue(service.getOpenRestaurants().isEmpty());
    }

    // R12: closes in 20 minutes
    @Test
    void warnsWhenClosingInLessThanThirtyMinutes() {
        clockAt("21:40");

        assertTrue(service.isClosingSoon(dayRestaurant));
    }

    // R13: closes in exactly 30 minutes
    @Test
    void noWarningWhenClosingInExactlyThirtyMinutes() {
        clockAt("21:30");

        assertFalse(service.isClosingSoon(dayRestaurant));
    }

    @Test
    void noWarningWhenClosingLater() {
        clockAt("12:00");

        assertFalse(service.isClosingSoon(dayRestaurant));
    }

    @Test
    void warnsForOvernightRestaurantJustBeforeClosing() {
        clockAt("01:45");

        assertTrue(service.isClosingSoon(nightRestaurant));
    }

    @Test
    void noWarningForClosedRestaurant() {
        clockAt("23:00");

        assertFalse(service.isClosingSoon(dayRestaurant));
    }

    @Test
    void rejectsNullRestaurantWhenCheckingClosingSoon() {
        assertThrows(IllegalArgumentException.class, () -> service.isClosingSoon(null));
    }

    @Test
    void rejectsNullRepository() {
        assertThrows(IllegalArgumentException.class, () -> new RestaurantService(null, timeProvider));
    }

    @Test
    void rejectsNullTimeProvider() {
        assertThrows(IllegalArgumentException.class, () -> new RestaurantService(repository, null));
    }
}
