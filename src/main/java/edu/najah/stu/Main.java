package edu.najah.stu;

import edu.najah.stu.domain.Landmark;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.presentation.RestaurantFormatter;
import edu.najah.stu.repository.DataStore;
import edu.najah.stu.repository.FileRestaurantRepository;
import edu.najah.stu.repository.RestaurantImporter;
import edu.najah.stu.service.NearbyRestaurant;
import edu.najah.stu.service.RestaurantService;
import edu.najah.stu.service.SystemTimeProvider;

import java.nio.file.Path;
import java.util.List;

public class Main {

    private static final double SEARCH_RADIUS_KM = 1.5;
    private static final int NAMES_TO_SHOW = 10;
    private static final Path DATA_FOLDER = Path.of("data");

    public static void main(String[] args) {
        FileRestaurantRepository repository = DataStore.open(DATA_FOLDER,
                RestaurantImporter.withDefaultStrategies()::importBundledData);
        RestaurantService restaurantService = new RestaurantService(repository, new SystemTimeProvider());

        System.out.println("Loaded " + repository.findAll().size() + " restaurants in Nablus from "
                + DATA_FOLDER.toAbsolutePath());
        for (Landmark landmark : Landmark.values()) {
            List<NearbyRestaurant> results = restaurantService.findNearby(landmark.getLocation(), SEARCH_RADIUS_KM);
            System.out.println();
            System.out.println("Open restaurants within " + SEARCH_RADIUS_KM + " km of "
                    + landmark.getDisplayName() + ":");
            if (results.isEmpty()) {
                System.out.println("   (none open right now)");
            }
            results.forEach(result -> System.out.println(" * " + RestaurantFormatter.format(result)));
        }

        List<Restaurant> openWithoutLocation = restaurantService.getOpenRestaurants().stream()
                .filter(restaurant -> restaurant.getLocation().isEmpty())
                .toList();
        System.out.println();
        System.out.println("Also open now, location not known yet (" + openWithoutLocation.size() + "):");
        openWithoutLocation.stream().limit(NAMES_TO_SHOW)
                .forEach(restaurant -> System.out.println(" * " + restaurant.getName()));
        if (openWithoutLocation.size() > NAMES_TO_SHOW) {
            System.out.println("   ... and " + (openWithoutLocation.size() - NAMES_TO_SHOW) + " more");
        }
        System.out.println();
        System.out.println("Map data (c) OpenStreetMap contributors");
    }
}
