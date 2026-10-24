package edu.najah.stu.service;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock RestaurantRepository repository;
    @InjectMocks RestaurantService service;

    private Restaurant restaurant(int id, boolean open) {
        return new Restaurant(id, "R" + id, open, new BigDecimal("20"), 5.0);
    }

    @Test
    void openRestaurantsAreDisplayed_closedAreHidden() {
        Restaurant open = restaurant(1, true);
        Restaurant closed = restaurant(2, false);
        when(repository.findAll()).thenReturn(Arrays.asList(open, closed));

        List<Restaurant> result = service.getOpenRestaurants();

        assertEquals(1, result.size());
        assertSame(open, result.get(0));
    }

    @Test
    void availableItemsDisplayed_unavailableHidden_priceCorrect() {
        Restaurant r = restaurant(1, true);
        r.addMenuItem(new MenuItem(1, "Shawarma", new BigDecimal("15.50"), true));
        r.addMenuItem(new MenuItem(2, "Falafel", new BigDecimal("8.00"), false));
        when(repository.findById(1)).thenReturn(Optional.of(r));

        List<MenuItem> menu = service.getAvailableMenu(1);

        assertEquals(1, menu.size());
        assertEquals("Shawarma", menu.get(0).getName());
        assertEquals(new BigDecimal("15.50"), menu.get(0).getPrice());
    }

    @Test
    void unknownRestaurantThrows() {
        when(repository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RestaurantNotFoundException.class, () -> service.getAvailableMenu(99));
    }

    @Test
    void negativePriceRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new MenuItem(1, "Bad", new BigDecimal("-1"), true));
    }
}