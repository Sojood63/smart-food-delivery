package edu.najah.stu.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantTest {

    private static final OpeningHours HOURS = new OpeningHours(LocalTime.of(9, 0), LocalTime.of(22, 0));
    private static final WeeklySchedule SCHEDULE = WeeklySchedule.everyDay(HOURS, HoursSource.MANUAL);
    private static final Location LOCATION = new Location(32.2189, 35.2400);

    @Test
    void createsRestaurantWithEmptyMenu() {
        Restaurant restaurant = new Restaurant("R1", "Abu Salem", SCHEDULE, LOCATION, Cuisine.OTHER);

        assertEquals("R1", restaurant.getId());
        assertEquals("Abu Salem", restaurant.getName());
        assertTrue(restaurant.getMenuItems().isEmpty());
    }

    @Test
    void addsMenuItem() {
        Restaurant restaurant = new Restaurant("R1", "Abu Salem", SCHEDULE, LOCATION, Cuisine.OTHER);
        MenuItem item = new MenuItem("M1", "Falafel", BigDecimal.ONE, true);

        restaurant.addMenuItem(item);

        assertEquals(List.of(item), restaurant.getMenuItems());
    }

    @Test
    void rejectsNullMenuItem() {
        Restaurant restaurant = new Restaurant("R1", "Abu Salem", SCHEDULE, LOCATION, Cuisine.OTHER);

        assertThrows(IllegalArgumentException.class, () -> restaurant.addMenuItem(null));
    }

    @Test
    void rejectsDuplicateMenuItem() {
        Restaurant restaurant = new Restaurant("R1", "Abu Salem", SCHEDULE, LOCATION, Cuisine.OTHER);
        restaurant.addMenuItem(new MenuItem("M1", "Falafel", BigDecimal.ONE, true));

        MenuItem duplicate = new MenuItem("M1", "Hummus", BigDecimal.TEN, true);

        assertThrows(IllegalArgumentException.class, () -> restaurant.addMenuItem(duplicate));
        assertEquals(1, restaurant.getMenuItems().size());
    }

    @Test
    void menuCannotBeModifiedFromOutside() {
        Restaurant restaurant = new Restaurant("R1", "Abu Salem", SCHEDULE, LOCATION, Cuisine.OTHER);
        List<MenuItem> menu = restaurant.getMenuItems();
        MenuItem item = new MenuItem("M1", "Falafel", BigDecimal.ONE, true);

        assertThrows(UnsupportedOperationException.class, () -> menu.add(item));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsMissingId(String id) {
        assertThrows(IllegalArgumentException.class, () -> new Restaurant(id, "Abu Salem", SCHEDULE, LOCATION, Cuisine.OTHER));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsMissingName(String name) {
        assertThrows(IllegalArgumentException.class, () -> new Restaurant("R1", name, SCHEDULE, LOCATION, Cuisine.OTHER));
    }

    @Test
    void rejectsMissingSchedule() {
        assertThrows(IllegalArgumentException.class,
                () -> new Restaurant("R1", "Abu Salem", null, LOCATION, Cuisine.OTHER));
    }

    @Test
    void keepsOpeningHours() {
        Restaurant restaurant = new Restaurant("R1", "Abu Salem", SCHEDULE, LOCATION, Cuisine.OTHER);

        assertSame(SCHEDULE, restaurant.getSchedule());
    }

    @Test
    void isOpenOnlyDuringItsHours() {
        Restaurant restaurant = new Restaurant("R1", "Abu Salem", SCHEDULE, LOCATION, Cuisine.OTHER);

        assertTrue(restaurant.isOpenAt(LocalDateTime.of(2026, 10, 30, 12, 0)));
        assertFalse(restaurant.isOpenAt(LocalDateTime.of(2026, 10, 30, 23, 0)));
    }

    @Test
    void allowsUnknownLocation() {
        Restaurant restaurant = new Restaurant("R1", "Abu Salem", SCHEDULE, null, Cuisine.OTHER);

        assertTrue(restaurant.getLocation().isEmpty());
    }

    @Test
    void rejectsMissingCuisine() {
        assertThrows(IllegalArgumentException.class,
                () -> new Restaurant("R1", "Abu Salem", SCHEDULE, LOCATION, null));
    }

    @Test
    void keepsLocationAndCuisine() {
        Restaurant restaurant = new Restaurant("R1", "Abu Salem", SCHEDULE, LOCATION, Cuisine.SWEETS);

        assertSame(LOCATION, restaurant.getLocation().orElseThrow());
        assertEquals(Cuisine.SWEETS, restaurant.getCuisine());
    }
}
