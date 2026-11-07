package edu.najah.stu.service;

import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.repository.RestaurantRepository;

import java.util.List;

/**
 * Gives customers the menu of a restaurant (US1.3).
 *
 * <p>Only the items that can be ordered now are returned; items the manager
 * marked as unavailable are hidden.</p>
 */
public class MenuService {

    /** Where restaurants and their menus are stored. */
    private final RestaurantRepository restaurantRepository;

    /**
     * Creates the service.
     *
     * @param restaurantRepository where restaurants are stored; must not be null
     * @throws IllegalArgumentException if the repository is null
     */
    public MenuService(RestaurantRepository restaurantRepository) {
        if (restaurantRepository == null) {
            throw new IllegalArgumentException("Restaurant repository must not be null");
        }
        this.restaurantRepository = restaurantRepository;
    }

    /**
     * Returns the items of a restaurant that can be ordered now, in menu order.
     *
     * @param restaurantId the restaurant id; must not be empty
     * @return the available items; empty if nothing can be ordered
     * @throws IllegalArgumentException    if the id is null or empty
     * @throws RestaurantNotFoundException if no restaurant has that id
     */
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
