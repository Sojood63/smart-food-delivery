package edu.najah.stu.repository;

import edu.najah.stu.domain.Restaurant;
import java.util.List;
import java.util.Optional;

public interface RestaurantRepository {
    List<Restaurant> findAll();
    Optional<Restaurant> findById(int id);
    void save(Restaurant restaurant);
}