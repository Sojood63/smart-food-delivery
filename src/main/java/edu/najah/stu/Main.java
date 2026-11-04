package edu.najah.stu;

import edu.najah.stu.domain.Landmark;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.presentation.ManagerConsole;
import edu.najah.stu.presentation.RestaurantFormatter;
import edu.najah.stu.repository.DataStore;
import edu.najah.stu.repository.FileRestaurantRepository;
import edu.najah.stu.repository.RestaurantImporter;
import edu.najah.stu.service.NearbyRestaurant;
import edu.najah.stu.service.RestaurantManagementService;
import edu.najah.stu.service.RestaurantService;
import edu.najah.stu.service.SystemTimeProvider;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final double SEARCH_RADIUS_KM = 1.5;
    private static final int NAMES_TO_SHOW = 10;
    private static final Path DATA_FOLDER = Path.of("data");
    private static final String MAIN_MENU = String.join(System.lineSeparator(),
            "",
            "=== Smart Food Delivery ===",
            "1) Open restaurants near An-Najah",
            "2) Manager panel",
            "3) Reload the bundled data (deletes all manager changes)",
            "0) Exit");

    public static void main(String[] args) {
        RestaurantImporter importer = RestaurantImporter.withDefaultStrategies();
        FileRestaurantRepository repository = DataStore.open(DATA_FOLDER, importer::importBundledData);
        RestaurantService restaurantService = new RestaurantService(repository, new SystemTimeProvider());
        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
        ManagerConsole managerConsole = new ManagerConsole(
                new RestaurantManagementService(repository), in, System.out);

        System.out.println("Loaded " + repository.findAll().size() + " restaurants in Nablus from "
                + DATA_FOLDER.toAbsolutePath());
       while (true) {
            System.out.println(MAIN_MENU);
            String choice = in.hasNextLine() ? in.nextLine().trim() : "0";
            switch (choice) {
                case "1" -> printNearby(restaurantService);
                case "2" -> managerConsole.run();
                case "3" -> reload(repository, importer, in);
                case "0" -> {
                    System.out.println("Map data (c) OpenStreetMap contributors");
                    return;
                }
                default -> System.out.println("Unknown choice: " + choice);
            }
        }
    }

    private static void reload(FileRestaurantRepository repository, RestaurantImporter importer, Scanner in) {
        System.out.println("This deletes every change made in the manager panel. Type yes to continue:");
        if (in.hasNextLine() && in.nextLine().trim().equalsIgnoreCase("yes")) {
            repository.replaceAll(importer.importBundledData());
            System.out.println("Reloaded " + repository.findAll().size() + " restaurants.");
        } else {
            System.out.println("Nothing changed.");
        }
    }

    private static void printNearby(RestaurantService restaurantService) {
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
    }
}
