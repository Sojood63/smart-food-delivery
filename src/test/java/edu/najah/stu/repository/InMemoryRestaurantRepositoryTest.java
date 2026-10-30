package edu.najah.stu.repository;

import edu.najah.stu.domain.OpeningHours;
import edu.najah.stu.domain.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRestaurantRepositoryTest {

    private static final OpeningHours HOURS = new OpeningHours(LocalTime.of(9, 0), LocalTime.of(22, 0));

    private InMemoryRestaurantRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRestaurantRepository();
    }

    @Test
    void findsSavedRestaurant() {
        Restaurant restaurant = new Restaurant("R1", "Abu Salem", HOURS);
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
        repository.save(new Restaurant("R1", "Old Name", HOURS));
        Restaurant updated = new Restaurant("R1", "New Name", HOURS);

        repository.save(updated);

        assertSame(updated, repository.findById("R1").orElseThrow());
    }

    @Test
    void rejectsNullRestaurant() {
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }

    @Test
    void findAllReturnsRestaurantsInSavingOrder() {
        Restaurant first = new Restaurant("R2", "First", HOURS);
        Restaurant second = new Restaurant("R1", "Second", HOURS);
        repository.save(first);
        repository.save(second);

        assertEquals(List.of(first, second), repository.findAll());
    }

    @Test
    void findAllIsEmptyWhenNothingSaved() {
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void findAllCannotBeModifiedFromOutside() {
        List<Restaurant> all = repository.findAll();
        Restaurant restaurant = new Restaurant("R1", "Abu Salem", HOURS);

        assertThrows(UnsupportedOperationException.class, () -> all.add(restaurant));
    }
}
