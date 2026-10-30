package edu.najah.stu.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantTest {

    private static final OpeningHours HOURS = new OpeningHours(LocalTime.of(9, 0), LocalTime.of(22, 0));

    @Test
    void createsRestaurantWithEmptyMenu() {
        Restaurant restaurant = new Restaurant("R1", "Abu Salem", HOURS);

        assertEquals("R1", restaurant.getId());
        assertEquals("Abu Salem", restaurant.getName());
        assertTrue(restaurant.getMenuItems().isEmpty());
    }

    @Test
    void addsMenuItem() {
        Restaurant restaurant = new Restaurant("R1", "Abu Salem", HOURS);
        MenuItem item = new MenuItem("M1", "Falafel", BigDecimal.ONE, true);

        restaurant.addMenuItem(item);

        assertEquals(List.of(item), restaurant.getMenuItems());
    }

    @Test
    void rejectsNullMenuItem() {
        Restaurant restaurant = new Restaurant("R1", "Abu Salem", HOURS);

        assertThrows(IllegalArgumentException.class, () -> restaurant.addMenuItem(null));
    }

    @Test
    void rejectsDuplicateMenuItem() {
        Restaurant restaurant = new Restaurant("R1", "Abu Salem", HOURS);
        restaurant.addMenuItem(new MenuItem("M1", "Falafel", BigDecimal.ONE, true));

        MenuItem duplicate = new MenuItem("M1", "Hummus", BigDecimal.TEN, true);

        assertThrows(IllegalArgumentException.class, () -> restaurant.addMenuItem(duplicate));
        assertEquals(1, restaurant.getMenuItems().size());
    }

    @Test
    void menuCannotBeModifiedFromOutside() {
        Restaurant restaurant = new Restaurant("R1", "Abu Salem", HOURS);
        List<MenuItem> menu = restaurant.getMenuItems();
        MenuItem item = new MenuItem("M1", "Falafel", BigDecimal.ONE, true);

        assertThrows(UnsupportedOperationException.class, () -> menu.add(item));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsMissingId(String id) {
        assertThrows(IllegalArgumentException.class, () -> new Restaurant(id, "Abu Salem", HOURS));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsMissingName(String name) {
        assertThrows(IllegalArgumentException.class, () -> new Restaurant("R1", name, HOURS));
    }

    @Test
    void rejectsMissingOpeningHours() {
        assertThrows(IllegalArgumentException.class, () -> new Restaurant("R1", "Abu Salem", null));
    }

    @Test
    void keepsOpeningHours() {
        Restaurant restaurant = new Restaurant("R1", "Abu Salem", HOURS);

        assertSame(HOURS, restaurant.getOpeningHours());
    }

    @Test
    void isOpenOnlyDuringItsHours() {
        Restaurant restaurant = new Restaurant("R1", "Abu Salem", HOURS);

        assertTrue(restaurant.isOpenAt(LocalTime.of(12, 0)));
        assertFalse(restaurant.isOpenAt(LocalTime.of(23, 0)));
    }
}
