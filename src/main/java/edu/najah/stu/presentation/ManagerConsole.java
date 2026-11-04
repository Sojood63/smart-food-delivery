package edu.najah.stu.presentation;

import edu.najah.stu.domain.HoursFormatter;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.service.InvalidHoursException;
import edu.najah.stu.service.MenuItemNotFoundException;
import edu.najah.stu.service.RestaurantManagementService;
import edu.najah.stu.service.RestaurantNotFoundException;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Scanner;

/**
 * The text menu a restaurant manager uses to change their restaurant's hours,
 * location and menu. A manager can only open their own restaurant.
 *
 * <p>It only reads and prints; every check and every save is done by
 * {@link RestaurantManagementService}. A mistake prints an error and returns
 * to the menu, so the manager can try again.</p>
 */
public class ManagerConsole {

    private static final String MENU = String.join(System.lineSeparator(),
            "",
            "1) Show details",
            "2) Change opening hours",
            "3) Change location",
            "4) Add menu item",
            "5) Change item price",
            "6) Make item available / unavailable",
            "7) Remove menu item",
            "0) Back");

    private final RestaurantManagementService service;
    private final ConsoleIO io;

    /**
     * Creates the console.
     *
     * @param service does the actual changes; must not be null
     * @param in      where the manager's answers are read from; must not be null
     * @param out     where text is printed; must not be null
     */
    public ManagerConsole(RestaurantManagementService service, Scanner in, PrintStream out) {
        if (service == null) {
            throw new IllegalArgumentException("Service must not be null");
        }
        this.service = service;
        this.io = new ConsoleIO(in, out);
    }

    /**
     * Shows the manager menu for one restaurant until the manager chooses "Back".
     *
     * @param restaurantId the restaurant the logged-in manager runs
     */
    public void run(String restaurantId) {
        Restaurant restaurant;
        try {
            restaurant = service.getRestaurant(restaurantId);
        } catch (IllegalArgumentException | RestaurantNotFoundException e) {
            io.println("Error: " + e.getMessage());
            return;
        }
        io.println("Managing " + restaurant.getName() + " (" + restaurant.getId() + ")");
        String choice = io.ask(MENU);
        while (choice != null && !choice.equals("0")) {
            handle(choice, restaurant);
            choice = io.ask(MENU);
        }
    }

    private void handle(String choice, Restaurant restaurant) {
        String id = restaurant.getId();
        try {
            switch (choice) {
                case "1" -> showDetails(restaurant);
                case "2" -> changeHours(id);
                case "3" -> changeLocation(id);
                case "4" -> addItem(id);
                case "5" -> changePrice(id);
                case "6" -> changeAvailability(id);
                case "7" -> removeItem(id);
                default -> io.println("Unknown choice: " + choice);
            }
        } catch (IllegalArgumentException | MenuItemNotFoundException e) {
            io.println("Error: " + e.getMessage());
        }
    }

    private void showDetails(Restaurant restaurant) {
        io.println(restaurant.getName() + " | " + restaurant.getCuisine().getDisplayName());
        io.println("Hours: " + describeHours(restaurant));
        io.println("Location: " + restaurant.getLocation().map(ManagerConsole::describe).orElse("not known yet"));
        if (restaurant.getMenuItems().isEmpty()) {
            io.println("Menu: empty");
        }
        for (MenuItem item : restaurant.getMenuItems()) {
            io.println("  " + item.getId() + " | " + item.getName() + " | " + item.getPrice().toPlainString()
                    + " ILS" + (item.isAvailable() ? "" : " | not available"));
        }
    }

    private static String describeHours(Restaurant restaurant) {
        HoursSource source = restaurant.getSchedule().getSource();
        if (source == HoursSource.UNKNOWN) {
            return "not known";
        }
        return HoursFormatter.format(restaurant.getSchedule()) + " (" + source.name().toLowerCase(Locale.ROOT) + ")";
    }

    private static String describe(Location location) {
        return location.getLatitude() + ", " + location.getLongitude();
    }

    private void changeHours(String id) {
        String text = io.answer("New hours (example: " + InvalidHoursException.EXAMPLE + ", or 24/7):");
        io.println("Saved. Hours: " + HoursFormatter.format(service.updateHours(id, text)));
    }

    private void changeLocation(String id) {
        double latitude = number(io.answer("Latitude (example: 32.2243):")).doubleValue();
        double longitude = number(io.answer("Longitude (example: 35.2434):")).doubleValue();
        io.println("Saved. Location: " + describe(service.updateLocation(id, latitude, longitude)));
    }

    private void addItem(String id) {
        String name = io.answer("Item name:");
        BigDecimal price = number(io.answer("Price in ILS:"));
        MenuItem item = service.addMenuItem(id, name, price);
        io.println("Added " + item.getName() + " as " + item.getId());
    }

    private void changePrice(String id) {
        String itemId = io.answer("Item id:");
        BigDecimal price = number(io.answer("New price in ILS:"));
        MenuItem item = service.changePrice(id, itemId, price);
        io.println("Saved. " + item.getName() + " now costs " + item.getPrice().toPlainString() + " ILS");
    }

    private void changeAvailability(String id) {
        String itemId = io.answer("Item id:");
        String reply = io.answer("Can customers order it now? (yes/no):").toLowerCase(Locale.ROOT);
        if (!reply.equals("yes") && !reply.equals("no")) {
            throw new IllegalArgumentException("Please answer yes or no");
        }
        MenuItem item = service.setAvailable(id, itemId, reply.equals("yes"));
        io.println("Saved. " + item.getName() + (item.isAvailable() ? " is available" : " is not available"));
    }

    private void removeItem(String id) {
        String itemId = io.answer("Item id to remove for good:");
        service.removeMenuItem(id, itemId);
        io.println("Removed " + itemId);
    }

    /** Reads a typed number such as {@code 12.5}, with a clear message for anything else. */
    private static BigDecimal number(String text) {
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + text + "' is not a number");
        }
    }
}
