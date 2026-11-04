package edu.najah.stu;

import edu.najah.stu.domain.Landmark;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.User;
import edu.najah.stu.presentation.LoginConsole;
import edu.najah.stu.presentation.ManagerConsole;
import edu.najah.stu.presentation.RestaurantFormatter;
import edu.najah.stu.repository.BundledUsers;
import edu.najah.stu.repository.DataStore;
import edu.najah.stu.repository.FileRestaurantRepository;
import edu.najah.stu.repository.FileUserRepository;
import edu.najah.stu.repository.RestaurantImporter;
import edu.najah.stu.service.AuthService;
import edu.najah.stu.service.NearbyRestaurant;
import edu.najah.stu.service.Pbkdf2PasswordHasher;
import edu.najah.stu.service.RestaurantManagementService;
import edu.najah.stu.service.RestaurantService;
import edu.najah.stu.service.SystemTimeProvider;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final double SEARCH_RADIUS_KM = 1.5;
    private static final int NAMES_TO_SHOW = 10;
    private static final Path DATA_FOLDER = Path.of("data");
    private static final String CUSTOMER_MENU = String.join(System.lineSeparator(),
            "",
            "1) Open restaurants near An-Najah",
            "0) Log out");
    private static final String MANAGER_MENU = String.join(System.lineSeparator(),
            "",
            "1) Manage my restaurant",
            "2) Open restaurants near An-Najah",
            "3) Reload the bundled data (deletes all manager changes)",
            "0) Log out");

    private final RestaurantImporter importer = RestaurantImporter.withDefaultStrategies();
    private final FileRestaurantRepository restaurants = DataStore.open(DATA_FOLDER, importer::importBundledData);
    private final RestaurantService restaurantService = new RestaurantService(restaurants, new SystemTimeProvider());
    private final Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
    private final ManagerConsole managerConsole =
            new ManagerConsole(new RestaurantManagementService(restaurants), in, System.out);

    public static void main(String[] args) {
        new Main().start();
    }

    private void start() {
        FileUserRepository users = DataStore.openUsers(DATA_FOLDER, BundledUsers::load);
        AuthService authService = new AuthService(users, new Pbkdf2PasswordHasher(), new SystemTimeProvider());
        LoginConsole loginConsole = new LoginConsole(authService, in, System.out);

        System.out.println("Loaded " + restaurants.findAll().size() + " restaurants and " + users.findAll().size()
                + " accounts from " + DATA_FOLDER.toAbsolutePath());
        Optional<User> user = loginConsole.run();
        while (user.isPresent()) {
            if (user.get().getRole() == Role.MANAGER) {
                managerMenu(user.get());
            } else {
                customerMenu();
            }
            System.out.println("Logged out.");
            user = loginConsole.run();
        }
        System.out.println("Map data (c) OpenStreetMap contributors");
    }

    private String ask(String menu) {
        System.out.println(menu);
        return in.hasNextLine() ? in.nextLine().trim() : "0";
    }

    private void customerMenu() {
        String choice = ask(CUSTOMER_MENU);
        while (!choice.equals("0")) {
            if (choice.equals("1")) {
                printNearby();
            } else {
                System.out.println("Unknown choice: " + choice);
            }
            choice = ask(CUSTOMER_MENU);
        }
    }

    private void managerMenu(User manager) {
        String restaurantId = manager.getRestaurantId().orElseThrow();
        String choice = ask(MANAGER_MENU);
        while (!choice.equals("0")) {
            switch (choice) {
                case "1" -> managerConsole.run(restaurantId);
                case "2" -> printNearby();
                case "3" -> reload();
                default -> System.out.println("Unknown choice: " + choice);
            }
            choice = ask(MANAGER_MENU);
        }
    }

    private void reload() {
        System.out.println("This deletes every change made in the manager panel. Type yes to continue:");
        if (in.hasNextLine() && in.nextLine().trim().equalsIgnoreCase("yes")) {
            restaurants.replaceAll(importer.importBundledData());
            System.out.println("Reloaded " + restaurants.findAll().size() + " restaurants.");
        } else {
            System.out.println("Nothing changed.");
        }
    }

    private void printNearby() {
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
