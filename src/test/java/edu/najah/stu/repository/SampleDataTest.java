package edu.najah.stu.repository;

import edu.najah.stu.domain.Landmark;
import edu.najah.stu.domain.Restaurant;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
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
    void containsThirteenRestaurants() {
        assertEquals(13, restaurants.size());
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
        LocalDateTime tuesdayAfterMidnight = LocalDateTime.of(2026, 11, 3, 0, 30);
        assertTrue(restaurants.stream().anyMatch(restaurant -> restaurant.isOpenAt(tuesdayAfterMidnight)));
    }

    @Test
    void createsAFreshRepositoryEachTime() {
        InMemoryRestaurantRepository first = SampleData.createRepository();
        InMemoryRestaurantRepository second = SampleData.createRepository();

        assertNotSame(first, second);
    }

    @Test
    void everyRestaurantIsInNablus() {
        restaurants.forEach(restaurant -> restaurant.getLocation().ifPresent(location -> assertTrue(
                location.distanceKmTo(Landmark.OLD_CAMPUS.getLocation()) < 10,
                restaurant.getName() + " is too far from Nablus")));
    }

    @Test
    void idsShowWhereTheDataCameFrom() {
        restaurants.forEach(restaurant -> assertTrue(
                restaurant.getId().startsWith("osm-") || restaurant.getId().startsWith("manual-")));
    }

    @Test
    void includesARestaurantWithoutLocation() {
        assertTrue(restaurants.stream().anyMatch(restaurant -> restaurant.getLocation().isEmpty()));
    }

    @Test
    void pizzaAlNajahIsClosedOnFriday() {
        Restaurant pizzaAlNajah = restaurants.stream()
                .filter(restaurant -> restaurant.getName().equals("Pizza Al Najah"))
                .findFirst().orElseThrow();

        assertFalse(pizzaAlNajah.isOpenAt(LocalDateTime.of(2026, 10, 30, 12, 0)));
        assertTrue(pizzaAlNajah.isOpenAt(LocalDateTime.of(2026, 10, 31, 12, 0)));
    }
}
