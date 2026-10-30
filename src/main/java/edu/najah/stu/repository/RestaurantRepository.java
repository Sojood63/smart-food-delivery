package edu.najah.stu.repository;

import edu.najah.stu.domain.Restaurant;

import java.util.Optional;

public interface RestaurantRepository {

    void save(Restaurant restaurant);

    Optional<Restaurant> findById(String id);
}
