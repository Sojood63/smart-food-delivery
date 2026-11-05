package edu.najah.stu.presentation;

import edu.najah.stu.domain.Landmark;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.service.MenuService;
import edu.najah.stu.service.NearbyRestaurant;
import edu.najah.stu.service.RestaurantService;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * The text menu a customer uses: open restaurants near the university (US1.2)
 * and the menu of an open restaurant (US1.3).
 *
 * <p>Only open restaurants are offered, and a menu shows only the items that
 * can be ordered now, each with its price.</p>
 */
public class CustomerConsole {

    /** Radius of the nearby search around each campus. */
    static final double SEARCH_RADIUS_KM = 1.5;

    private static final int NAMES_TO_SHOW = 10;
    private static final String MENU = String.join(System.lineSeparator(),
            "",
            "1) Open restaurants near An-Najah",
            "2) View a restaurant's menu",
            "0) Log out");

    private final RestaurantService restaurantService;
    private final MenuService menuService;
    private final ConsoleIO io;

    /**
     * Creates the console.
     *
     * @param restaurantService finds open and nearby restaurants; must not be null
     * @param menuService       gives the orderable items of a restaurant; must not be null
     * @param in                where answers are read from; must not be null
     * @param out               where text is printed; must not be null
     */
    public CustomerConsole(RestaurantService restaurantService, MenuService menuService, Scanner in, PrintStream out) {
        if (restaurantService == null || menuService == null) {
            throw new IllegalArgumentException("Restaurant and menu services must not be null");
        }
        this.restaurantService = restaurantService;
        this.menuService = menuService;
        this.io = new ConsoleIO(in, out);
    }

    /**
     * Shows the customer menu until the customer logs out.
     */
    public void run() {
        String choice = io.ask(MENU);
        while (choice != null && !choice.equals("0")) {
            switch (choice) {
                case "1" -> printNearby();
                case "2" -> viewMenu();
                default -> io.println("Unknown choice: " + choice);
            }
            choice = io.ask(MENU);
        }
    }

    /**
     * Prints the open restaurants near each campus, nearest first, then the
     * open restaurants whose location is not known yet.
     */
    public void printNearby() {
        for (Landmark landmark : Landmark.values()) {
            List<NearbyRestaurant> results = restaurantService.findNearby(landmark.getLocation(), SEARCH_RADIUS_KM);
            io.println("");
            io.println("Open restaurants within " + SEARCH_RADIUS_KM + " km of " + landmark.getDisplayName() + ":");
            if (results.isEmpty()) {
                io.println("   (none open right now)");
            }
            results.forEach(result -> io.println(" * " + RestaurantFormatter.format(result)));
        }
        List<Restaurant> withoutLocation = restaurantService.getOpenRestaurants().stream()
                .filter(restaurant -> restaurant.getLocation().isEmpty())
                .toList();
        io.println("");
        io.println("Also open now, location not known yet (" + withoutLocation.size() + "):");
        withoutLocation.stream().limit(NAMES_TO_SHOW).forEach(restaurant -> io.println(" * " + restaurant.getName()));
        if (withoutLocation.size() > NAMES_TO_SHOW) {
            io.println("   ... and " + (withoutLocation.size() - NAMES_TO_SHOW) + " more");
        }
    }

    /**
     * Lists the open restaurants that have something to order, lets the
     * customer pick one, and prints its orderable items with prices.
     */
    void viewMenu() {
        List<Restaurant> withMenu = restaurantService.getOpenRestaurants().stream()
                .filter(restaurant -> !menuService.getAvailableMenu(restaurant.getId()).isEmpty())
                .toList();
        if (withMenu.isEmpty()) {
            io.println("No open restaurant has a menu right now.");
            return;
        }
        io.println("Open restaurants with a menu:");
        for (int i = 0; i < withMenu.size(); i++) {
            Restaurant restaurant = withMenu.get(i);
            io.println(" " + (i + 1) + ") " + restaurant.getName() + " | " + restaurant.getCuisine().getDisplayName());
        }
        String answer = io.answer("Choose a restaurant number, or press Enter to go back:");
        if (answer.isEmpty()) {
            return;
        }
        int number = parseNumber(answer);
        if (number < 1 || number > withMenu.size()) {
            io.println("Error: please choose a number from 1 to " + withMenu.size());
            return;
        }
        printMenu(withMenu.get(number - 1));
    }

    private void printMenu(Restaurant restaurant) {
        io.println("");
        io.println("Menu of " + restaurant.getName() + ":");
        for (MenuItem item : menuService.getAvailableMenu(restaurant.getId())) {
            io.println("  " + item.getName() + " | " + item.getPrice().toPlainString() + " ILS");
        }
    }

    /** Reads a whole number, or gives -1 for anything that is not one. */
    private static int parseNumber(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
