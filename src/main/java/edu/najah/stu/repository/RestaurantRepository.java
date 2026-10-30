package edu.najah.stu.repository;

import edu.najah.stu.domain.Restaurant;

import java.util.List;
import java.util.Optional;

/**
 * Stores and finds restaurants.
 */
public interface RestaurantRepository {

    /**
     * Saves a restaurant, replacing any restaurant with the same id.
     *
     * @param restaurant the restaurant to save; must not be null
     */
    void save(Restaurant restaurant);

    /**
     * Finds a restaurant by its id.
     *
     * @param id the restaurant id
     * @return the restaurant, or an empty optional if none has that id
     */
    Optional<Restaurant> findById(String id);

    /**
     * Returns every stored restaurant in the order they were first saved.
     *
     * @return an unmodifiable list of all restaurants
     */
    List<Restaurant> findAll();
}
