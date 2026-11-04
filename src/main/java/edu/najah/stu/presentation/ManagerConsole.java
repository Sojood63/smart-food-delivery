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
 * The text menu a restaurant manager uses to change hours, location and menu.
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
    private final Scanner in;
    private final PrintStream out;

    /**
     * Creates the console.
     *
     * @param service does the actual changes; must not be null
     * @param in      where the manager's answers are read from; must not be null
     * @param out     where text is printed; must not be null
     */
    public ManagerConsole(RestaurantManagementService service, Scanner in, PrintStream out) {
        if (service == null || in == null || out == null) {
            throw new IllegalArgumentException("Service, input and output must not be null");
        }
        this.service = service;
        this.in = in;
        this.out = out;
    }

    /**
     * Asks for a restaurant id and shows the manager menu until the manager chooses "Back".
     */
    public void run() {
        String restaurantId = ask("Restaurant id (for example manual-63), or press Enter to go back:");
        if (restaurantId == null || restaurantId.isEmpty()) {
            return;
        }
        Restaurant restaurant;
        try {
            restaurant = service.getRestaurant(restaurantId);
        } catch (RestaurantNotFoundException e) {
            out.println("Error: " + e.getMessage());
            return;
        }
        out.println("Managing " + restaurant.getName() + " (" + restaurant.getId() + ")");
        String choice = ask(MENU);
        while (choice != null && !choice.equals("0")) {
            handle(choice, restaurant);
            choice = ask(MENU);
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
                default -> out.println("Unknown choice: " + choice);
            }
        } catch (IllegalArgumentException | MenuItemNotFoundException e) {
            out.println("Error: " + e.getMessage());
        }
    }

    private void showDetails(Restaurant restaurant) {
        out.println(restaurant.getName() + " | " + restaurant.getCuisine().getDisplayName());
        out.println("Hours: " + describeHours(restaurant));
        out.println("Location: " + restaurant.getLocation().map(ManagerConsole::describe).orElse("not known yet"));
        if (restaurant.getMenuItems().isEmpty()) {
            out.println("Menu: empty");
        }
        for (MenuItem item : restaurant.getMenuItems()) {
            out.println("  " + item.getId() + " | " + item.getName() + " | " + item.getPrice().toPlainString()
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
        String text = answer("New hours (example: " + InvalidHoursException.EXAMPLE + ", or 24/7):");
        out.println("Saved. Hours: " + HoursFormatter.format(service.updateHours(id, text)));
    }

    private void changeLocation(String id) {
        double latitude = number(answer("Latitude (example: 32.2243):")).doubleValue();
        double longitude = number(answer("Longitude (example: 35.2434):")).doubleValue();
        out.println("Saved. Location: " + describe(service.updateLocation(id, latitude, longitude)));
    }

    private void addItem(String id) {
        String name = answer("Item name:");
        BigDecimal price = number(answer("Price in ILS:"));
        MenuItem item = service.addMenuItem(id, name, price);
        out.println("Added " + item.getName() + " as " + item.getId());
    }

    private void changePrice(String id) {
        String itemId = answer("Item id:");
        BigDecimal price = number(answer("New price in ILS:"));
        MenuItem item = service.changePrice(id, itemId, price);
        out.println("Saved. " + item.getName() + " now costs " + item.getPrice().toPlainString() + " ILS");
    }

    private void changeAvailability(String id) {
        String itemId = answer("Item id:");
        String reply = answer("Can customers order it now? (yes/no):").toLowerCase(Locale.ROOT);
        if (!reply.equals("yes") && !reply.equals("no")) {
            throw new IllegalArgumentException("Please answer yes or no");
        }
        MenuItem item = service.setAvailable(id, itemId, reply.equals("yes"));
        out.println("Saved. " + item.getName() + (item.isAvailable() ? " is available" : " is not available"));
    }

    private void removeItem(String id) {
        String itemId = answer("Item id to remove for good:");
        service.removeMenuItem(id, itemId);
        out.println("Removed " + itemId);
    }

    /** Reads a typed number such as {@code 12.5}, with a clear message for anything else. */
    private static BigDecimal number(String text) {
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + text + "' is not a number");
        }
    }

    /** Prints a prompt and reads one line; returns null when there is no more input. */
    private String ask(String prompt) {
        out.println(prompt);
        return in.hasNextLine() ? in.nextLine().trim() : null;
    }

    /** Like {@link #ask(String)}, but no input counts as an empty answer. */
    private String answer(String prompt) {
        String reply = ask(prompt);
        return reply == null ? "" : reply;
    }
}
