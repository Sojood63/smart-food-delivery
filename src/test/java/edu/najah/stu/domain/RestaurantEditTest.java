package edu.najah.stu.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The changes a manager can make to a restaurant.
 */
class RestaurantEditTest {

    private static final LocalDateTime MONDAY_NOON = LocalDateTime.of(2026, 11, 2, 12, 0);

    private Restaurant restaurant;
    private final MenuItem falafel = new MenuItem("R1-1", "Falafel", new BigDecimal("5"), true);
    private final MenuItem hummus = new MenuItem("R1-2", "Hummus", new BigDecimal("10"), true);

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant("R1", "Abu Salem", WeeklySchedule.unknown(), null, Cuisine.PALESTINIAN);
        restaurant.addMenuItem(falafel);
        restaurant.addMenuItem(hummus);
    }

    @Test
    void newScheduleIsUsedRightAway() {
        assertFalse(restaurant.isOpenAt(MONDAY_NOON));

        restaurant.changeSchedule(WeeklySchedule.allDay(HoursSource.MANUAL));

        assertTrue(restaurant.isOpenAt(MONDAY_NOON));
    }

    @Test
    void rejectsMissingSchedule() {
        assertThrows(IllegalArgumentException.class, () -> restaurant.changeSchedule(null));
    }

    @Test
    void restaurantWithoutLocationCanGetOne() {
        Location shop = new Location(32.2243, 35.2434);

        restaurant.moveTo(shop);

        assertEquals(Optional.of(shop), restaurant.getLocation());
    }

    @Test
    void rejectsMissingLocation() {
        assertThrows(IllegalArgumentException.class, () -> restaurant.moveTo(null));
    }

    @Test
    void replacedItemKeepsItsPlaceOnTheMenu() {
        MenuItem cheaperFalafel = falafel.withPrice(new BigDecimal("4"));

        restaurant.replaceMenuItem(cheaperFalafel);

        assertEquals(List.of("R1-1", "R1-2"), restaurant.getMenuItems().stream().map(MenuItem::getId).toList());
        assertEquals(new BigDecimal("4.00"), restaurant.getMenuItems().get(0).getPrice());
    }

    @Test
    void cannotReplaceAnItemThatIsNotOnTheMenu() {
        MenuItem stranger = new MenuItem("R1-9", "Kunafa", new BigDecimal("8"), true);

        assertThrows(IllegalArgumentException.class, () -> restaurant.replaceMenuItem(stranger));
        assertThrows(IllegalArgumentException.class, () -> restaurant.replaceMenuItem(null));
    }

    @Test
    void removesAnItemById() {
        assertTrue(restaurant.removeMenuItem("R1-1"));

        assertEquals(List.of(hummus), restaurant.getMenuItems());
    }

    @Test
    void removingAMissingItemChangesNothing() {
        assertFalse(restaurant.removeMenuItem("R1-9"));

        assertEquals(2, restaurant.getMenuItems().size());
    }

    @Test
    void findsAnItemById() {
        assertEquals(Optional.of(hummus), restaurant.findMenuItem("R1-2"));
        assertEquals(Optional.empty(), restaurant.findMenuItem("R1-9"));
    }
}
