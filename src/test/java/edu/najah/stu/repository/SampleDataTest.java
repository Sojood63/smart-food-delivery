package edu.najah.stu.repository;

import edu.najah.stu.domain.Restaurant;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SampleDataTest {

    private final List<Restaurant> restaurants = SampleData.createRepository().findAll();

    @Test
    void containsSixRestaurants() {
        assertEquals(6, restaurants.size());
    }

    @Test
    void restaurantIdsAreUnique() {
        Set<String> ids = restaurants.stream().map(Restaurant::getId).collect(Collectors.toSet());

        assertEquals(restaurants.size(), ids.size());
    }

    @Test
    void everyRestaurantHasAMenu() {
        restaurants.forEach(restaurant -> assertFalse(restaurant.getMenuItems().isEmpty()));
    }

    @Test
    void includesAnOvernightRestaurant() {
        assertTrue(restaurants.stream().anyMatch(restaurant -> restaurant.isOpenAt(LocalTime.of(0, 30))));
    }

    @Test
    void createsAFreshRepositoryEachTime() {
        InMemoryRestaurantRepository first = SampleData.createRepository();
        InMemoryRestaurantRepository second = SampleData.createRepository();

        assertNotSame(first, second);
    }
}
