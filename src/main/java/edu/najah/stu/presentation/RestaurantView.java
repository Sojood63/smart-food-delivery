package edu.najah.stu.presentation;

import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.service.RestaurantService;
import java.io.PrintStream;
import java.util.List;


public class RestaurantView {
    private final RestaurantService service;
    private final PrintStream out;

    public RestaurantView(RestaurantService service, PrintStream out) {
        this.service = service;
        this.out = out;
    }


    public void showRestaurants() {
        List<Restaurant> restaurants = service.getOpenRestaurants();
        if (restaurants.isEmpty()) {
            out.println("No open restaurants right now.");
            return;
        }
        out.println("=== Open Restaurants ===");
        for (Restaurant r : restaurants) {
            out.printf("[%d] %s%n", r.getId(), r.getName());
        }
    }


    public void showMenu(int restaurantId) {
        List<MenuItem> items = service.getAvailableMenu(restaurantId);
        if (items.isEmpty()) {
            out.println("No available items.");
            return;
        }
        out.println("=== Menu ===");
        for (MenuItem item : items) {
            out.printf("%d. %s - %s NIS%n", item.getId(), item.getName(),
                    item.getPrice().setScale(2));
        }
    }
}