package edu.najah.stu.repository;

import edu.najah.stu.domain.Restaurant;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Keeps restaurants in memory only; everything is lost when the program ends.
 * Used by tests, which need a fast repository with no files.
 */
public class InMemoryRestaurantRepository implements RestaurantRepository {

    /** The restaurants by id, in the order they were first saved. */
    private final Map<String, Restaurant> restaurants = new LinkedHashMap<>();

    @Override
    public void save(Restaurant restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant must not be null");
        }
        restaurants.put(restaurant.getId(), restaurant);
    }

    @Override
    public Optional<Restaurant> findById(String id) {
        return Optional.ofNullable(restaurants.get(id));
    }

    @Override
    public List<Restaurant> findAll() {
        return List.copyOf(restaurants.values());
    }
}
