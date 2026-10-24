package edu.najah.stu;

import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.presentation.RestaurantView;
import edu.najah.stu.repository.InMemoryRestaurantRepository;
import edu.najah.stu.service.RestaurantService;

import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        InMemoryRestaurantRepository repo = new InMemoryRestaurantRepository();

        Restaurant r = new Restaurant(1, "Al-Quds Grill", true, new BigDecimal("20"), 5);
        r.addMenuItem(new MenuItem(1, "Shawarma", new BigDecimal("15.50"), true));
        r.addMenuItem(new MenuItem(2, "Falafel", new BigDecimal("8"), false));
        repo.save(r);
        repo.save(new Restaurant(2, "Closed Place", false, new BigDecimal("20"), 5));

        RestaurantView view = new RestaurantView(new RestaurantService(repo), System.out);
        view.showRestaurants();
        view.showMenu(1);
    }
}