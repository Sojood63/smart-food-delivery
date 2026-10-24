package edu.najah.stu.repository;

import edu.najah.stu.domain.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRestaurantRepositoryTest {

    private InMemoryRestaurantRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRestaurantRepository();
    }

    private Restaurant restaurant(int id, String name) {
        return new Restaurant(id, name, true, new BigDecimal("20"), 5.0);
    }

    @Test
    void findAllReturnsEmptyListWhenNothingSaved() {
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void savedRestaurantCanBeFoundById() {
        Restaurant r = restaurant(1, "Al-Quds Grill");
        repository.save(r);

        Optional<Restaurant> found = repository.findById(1);

        assertTrue(found.isPresent());
        assertSame(r, found.get());
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertFalse(repository.findById(99).isPresent());
    }

    @Test
    void findAllReturnsAllSavedRestaurants() {
        repository.save(restaurant(1, "A"));
        repository.save(restaurant(2, "B"));

        List<Restaurant> all = repository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void savingSameIdReplacesOldRestaurant() {
        repository.save(restaurant(1, "Old"));
        repository.save(restaurant(1, "New"));

        assertEquals(1, repository.findAll().size());
        assertEquals("New", repository.findById(1).get().getName());
    }
}