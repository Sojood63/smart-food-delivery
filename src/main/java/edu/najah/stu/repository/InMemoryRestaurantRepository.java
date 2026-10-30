package edu.najah.stu.repository;

import edu.najah.stu.domain.Restaurant;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryRestaurantRepository implements RestaurantRepository {

    private final Map<String, Restaurant> restaurants = new HashMap<>();

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
}
