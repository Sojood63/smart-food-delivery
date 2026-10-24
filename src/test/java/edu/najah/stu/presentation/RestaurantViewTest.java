package edu.najah.stu.presentation;

import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantViewTest {

    @Mock
    RestaurantService service;

    private ByteArrayOutputStream buffer;
    private RestaurantView view;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
        view = new RestaurantView(service, new PrintStream(buffer));
    }

    private String output() {
        return buffer.toString();
    }

    @Test
    void showRestaurantsPrintsOpenRestaurants() {
        Restaurant r = new Restaurant(1, "Al-Quds Grill", true, new BigDecimal("20"), 5.0);
        when(service.getOpenRestaurants()).thenReturn(List.of(r));

        view.showRestaurants();

        assertTrue(output().contains("Open Restaurants"));
        assertTrue(output().contains("[1] Al-Quds Grill"));
    }

    @Test
    void showRestaurantsPrintsMessageWhenNoneOpen() {
        when(service.getOpenRestaurants()).thenReturn(Collections.emptyList());

        view.showRestaurants();

        assertTrue(output().contains("No open restaurants right now."));
    }

    @Test
    void showMenuPrintsItemNameAndPrice() {
        MenuItem item = new MenuItem(1, "Shawarma", new BigDecimal("15.5"), true);
        when(service.getAvailableMenu(1)).thenReturn(List.of(item));

        view.showMenu(1);

        assertTrue(output().contains("Shawarma"));
        assertTrue(output().contains("15.50 NIS"));
    }

    @Test
    void showMenuPrintsMessageWhenNoItems() {
        when(service.getAvailableMenu(1)).thenReturn(Collections.emptyList());

        view.showMenu(1);

        assertTrue(output().contains("No available items."));
    }
}