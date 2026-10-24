package edu.najah.stu.repository;

import edu.najah.stu.domain.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SampleDataTest {

    private InMemoryRestaurantRepository repository;

    @BeforeEach
    void setUp() {
        repository = SampleData.createRepository();
    }

    @Test
    void containsSevenRestaurants() {
        assertEquals(7, repository.findAll().size());
    }

    @Test
    void fiveRestaurantsAreOpenAndTwoAreClosed() {
        long open = repository.findAll().stream().filter(Restaurant::isOpen).count();

        assertEquals(5, open);
    }

    @Test
    void everyOpenRestaurantHasMenuItems() {
        for (Restaurant r : repository.findAll()) {
            if (r.isOpen()) {
                assertFalse(r.getMenu().isEmpty(), r.getName() + " should have a menu");
            }
        }
    }

    @Test
    void grillHasAnUnavailableItem() {
        Restaurant grill = repository.findById(1).orElseThrow();

        assertEquals("Al-Quds Grill", grill.getName());
        assertTrue(grill.getMenu().stream().anyMatch(i -> !i.isAvailable()));
    }
}