package edu.najah.stu.service;

import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.repository.RestaurantRepository;

import java.util.List;

public class MenuService {

    private final RestaurantRepository restaurantRepository;

    public MenuService(RestaurantRepository restaurantRepository) {
        if (restaurantRepository == null) {
            throw new IllegalArgumentException("Restaurant repository must not be null");
        }
        this.restaurantRepository = restaurantRepository;
    }

    public List<MenuItem> getAvailableMenu(String restaurantId) {
        if (restaurantId == null || restaurantId.isBlank()) {
            throw new IllegalArgumentException("Restaurant id must not be empty");
        }
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        return restaurant.getMenuItems().stream()
                .filter(MenuItem::isAvailable)
                .toList();
    }
}
