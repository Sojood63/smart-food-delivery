package edu.najah.stu.repository;

import edu.najah.stu.domain.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRestaurantRepositoryTest {

    private InMemoryRestaurantRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRestaurantRepository();
    }

    @Test
    void findsSavedRestaurant() {
        Restaurant restaurant = new Restaurant("R1", "Abu Salem");
        repository.save(restaurant);

        Optional<Restaurant> found = repository.findById("R1");

        assertTrue(found.isPresent());
        assertSame(restaurant, found.get());
    }

    @Test
    void returnsEmptyForUnknownId() {
        assertTrue(repository.findById("UNKNOWN").isEmpty());
    }

    @Test
    void savingSameIdReplacesOldRestaurant() {
        repository.save(new Restaurant("R1", "Old Name"));
        Restaurant updated = new Restaurant("R1", "New Name");

        repository.save(updated);

        assertSame(updated, repository.findById("R1").orElseThrow());
    }

    @Test
    void rejectsNullRestaurant() {
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }
}
