package edu.najah.stu.service;

import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.repository.RestaurantRepository;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for browsing restaurants and their menus.
 */
public class RestaurantService {
    private final RestaurantRepository repository;

    public RestaurantService(RestaurantRepository repository) {
        this.repository = repository;
    }

    /** US1.2: returns only open restaurants. */
    public List<Restaurant> getOpenRestaurants() {
        return repository.findAll().stream()
                .filter(Restaurant::isOpen)
                .collect(Collectors.toList());
    }

    /**
     * US1.3: returns only available menu items of the given restaurant.
     *
     * @throws RestaurantNotFoundException if the restaurant does not exist
     */
    public List<MenuItem> getAvailableMenu(int restaurantId) {
        Restaurant restaurant = repository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
        return restaurant.getMenu().stream()
                .filter(MenuItem::isAvailable)
                .collect(Collectors.toList());
    }
}