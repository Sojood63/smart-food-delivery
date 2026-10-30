package edu.najah.stu;

import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.repository.InMemoryRestaurantRepository;
import edu.najah.stu.service.MenuService;

import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        InMemoryRestaurantRepository repository = new InMemoryRestaurantRepository();
        MenuService menuService = new MenuService(repository);

        Restaurant restaurant = new Restaurant("R1", "Abu Salem");
        restaurant.addMenuItem(new MenuItem("M1", "Falafel", new BigDecimal("5"), true));
        restaurant.addMenuItem(new MenuItem("M2", "Hummus", new BigDecimal("8.5"), false));
        restaurant.addMenuItem(new MenuItem("M3", "Shawarma", new BigDecimal("15.75"), true));
        repository.save(restaurant);

        System.out.println("Menu of " + restaurant.getName() + ":");
        for (MenuItem item : menuService.getAvailableMenu("R1")) {
            System.out.println("- " + item.getName() + "  " + item.getPrice() + " NIS");
        }
    }
}