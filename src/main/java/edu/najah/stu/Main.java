package edu.najah.stu;

import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.repository.InMemoryRestaurantRepository;
import edu.najah.stu.repository.SampleData;
import edu.najah.stu.service.MenuService;
import edu.najah.stu.service.RestaurantService;
import edu.najah.stu.service.SystemTimeProvider;

public class Main {
    public static void main(String[] args) {
        InMemoryRestaurantRepository repository = SampleData.createRepository();
        RestaurantService restaurantService = new RestaurantService(repository, new SystemTimeProvider());
        MenuService menuService = new MenuService(repository);

        System.out.println("Open restaurants in Nablus now:");
        for (Restaurant restaurant : restaurantService.getOpenRestaurants()) {
            String warning = restaurantService.isClosingSoon(restaurant) ? "  (closing soon!)" : "";
            System.out.println();
            System.out.println("== " + restaurant.getName() + warning);
            for (MenuItem item : menuService.getAvailableMenu(restaurant.getId())) {
                System.out.println("   - " + item.getName() + "  " + item.getPrice() + " NIS");
            }
        }
    }
}
