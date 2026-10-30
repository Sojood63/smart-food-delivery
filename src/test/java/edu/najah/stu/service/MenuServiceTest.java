package edu.najah.stu.service;

import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.repository.InMemoryRestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MenuServiceTest {

    private InMemoryRestaurantRepository repository;
    private MenuService menuService;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRestaurantRepository();
        menuService = new MenuService(repository);
        restaurant = new Restaurant("R1", "Abu Salem");
        repository.save(restaurant);
    }

    @Test
    void showsAvailableItemsAndHidesUnavailableOnes() {
        MenuItem falafel = new MenuItem("M1", "Falafel", new BigDecimal("5.00"), true);
        MenuItem hummus = new MenuItem("M2", "Hummus", new BigDecimal("8.00"), false);
        MenuItem shawarma = new MenuItem("M3", "Shawarma", new BigDecimal("15.00"), true);
        restaurant.addMenuItem(falafel);
        restaurant.addMenuItem(hummus);
        restaurant.addMenuItem(shawarma);

        List<MenuItem> menu = menuService.getAvailableMenu("R1");

        assertEquals(List.of(falafel, shawarma), menu);
        assertFalse(menu.contains(hummus));
    }

    @Test
    void returnsItemPricesCorrectly() {
        restaurant.addMenuItem(new MenuItem("M1", "Falafel", new BigDecimal("12.5"), true));

        MenuItem item = menuService.getAvailableMenu("R1").get(0);

        assertEquals(new BigDecimal("12.50"), item.getPrice());
    }

    @Test
    void returnsEmptyListWhenAllItemsUnavailable() {
        restaurant.addMenuItem(new MenuItem("M1", "Falafel", BigDecimal.ONE, false));

        assertTrue(menuService.getAvailableMenu("R1").isEmpty());
    }

    @Test
    void returnsEmptyListWhenMenuIsEmpty() {
        assertTrue(menuService.getAvailableMenu("R1").isEmpty());
    }

    @Test
    void throwsWhenRestaurantDoesNotExist() {
        RestaurantNotFoundException exception = assertThrows(RestaurantNotFoundException.class,
                () -> menuService.getAvailableMenu("UNKNOWN"));

        assertEquals("Restaurant not found: UNKNOWN", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsMissingRestaurantId(String restaurantId) {
        assertThrows(IllegalArgumentException.class,
                () -> menuService.getAvailableMenu(restaurantId));
    }

    @Test
    void rejectsNullRepository() {
        assertThrows(IllegalArgumentException.class, () -> new MenuService(null));
    }
}
