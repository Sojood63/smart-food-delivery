package edu.najah.stu;

import edu.najah.stu.domain.Landmark;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.presentation.RestaurantFormatter;
import edu.najah.stu.repository.InMemoryRestaurantRepository;
import edu.najah.stu.repository.SampleData;
import edu.najah.stu.service.MenuService;
import edu.najah.stu.service.NearbyRestaurant;
import edu.najah.stu.service.RestaurantService;
import edu.najah.stu.service.SystemTimeProvider;

import java.util.List;

public class Main {

    private static final double SEARCH_RADIUS_KM = 1.5;

    public static void main(String[] args) {
        InMemoryRestaurantRepository repository = SampleData.createRepository();
        RestaurantService restaurantService = new RestaurantService(repository, new SystemTimeProvider());
        MenuService menuService = new MenuService(repository);

        for (Landmark landmark : Landmark.values()) {
            List<NearbyRestaurant> results = restaurantService.findNearby(landmark.getLocation(), SEARCH_RADIUS_KM);
            System.out.println();
            System.out.println("Open restaurants within " + SEARCH_RADIUS_KM + " km of "
                    + landmark.getDisplayName() + ":");
            if (results.isEmpty()) {
                System.out.println("   (none open right now)");
            }
            for (NearbyRestaurant result : results) {
                System.out.println(" * " + RestaurantFormatter.format(result));
                for (MenuItem item : menuService.getAvailableMenu(result.restaurant().getId())) {
                    System.out.println("      - " + item.getName() + "  " + item.getPrice() + " NIS");
                }
            }
        }
        System.out.println();
        System.out.println("Also open now (location not known yet):");
        restaurantService.getOpenRestaurants().stream()
                .filter(restaurant -> restaurant.getLocation().isEmpty())
                .forEach(restaurant -> System.out.println(" * " + restaurant.getName()));
        System.out.println();
        System.out.println("Map data (c) OpenStreetMap contributors");
    }
}
