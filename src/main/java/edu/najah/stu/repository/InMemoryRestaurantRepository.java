package edu.najah.stu.repository;

import edu.najah.stu.domain.Restaurant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** In-memory implementation of {@link RestaurantRepository}. */
public class InMemoryRestaurantRepository implements RestaurantRepository {
    private final Map<Integer, Restaurant> store = new LinkedHashMap<>();

    @Override
    public List<Restaurant> findAll() { return new ArrayList<>(store.values()); }

    @Override
    public Optional<Restaurant> findById(int id) { return Optional.ofNullable(store.get(id)); }

    @Override
    public void save(Restaurant restaurant) { store.put(restaurant.getId(), restaurant); }
}