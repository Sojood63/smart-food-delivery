package edu.najah.stu.service;

import edu.najah.stu.domain.HoursParser;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.Landmark;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.WeeklySchedule;
import edu.najah.stu.repository.RestaurantRepository;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.function.UnaryOperator;

/**
 * Lets a restaurant's manager change its opening hours, location and menu.
 *
 * <p>Every change is checked first and saved right away, so it is still
 * there after the program restarts. A rejected change leaves the restaurant
 * as it was and saves nothing.</p>
 */
public class RestaurantManagementService {

    /** A location further than this from the university is treated as a typing mistake. */
    static final double MAX_KM_FROM_NABLUS = 10;

    private static final int MAX_ITEM_NUMBER_DIGITS = 9;

    private final RestaurantRepository restaurantRepository;

    /**
     * Creates the service.
     *
     * @param restaurantRepository where restaurants are stored; must not be null
     */
    public RestaurantManagementService(RestaurantRepository restaurantRepository) {
        if (restaurantRepository == null) {
            throw new IllegalArgumentException("Restaurant repository must not be null");
        }
        this.restaurantRepository = restaurantRepository;
    }

    /**
     * Finds the restaurant the manager wants to work on.
     *
     * @param restaurantId the restaurant id; must not be empty
     * @return the restaurant
     * @throws RestaurantNotFoundException if no restaurant has that id
     */
    public Restaurant getRestaurant(String restaurantId) {
        if (restaurantId == null || restaurantId.isBlank()) {
            throw new IllegalArgumentException("Restaurant id must not be empty");
        }
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
    }

    /**
     * Sets new opening hours typed by the manager. The hours are marked as
     * coming from the manager, so they win over the map and estimates.
     *
     * @param restaurantId the restaurant to change
     * @param hoursText    the hours, for example {@code "Sa-Th 09:00-23:00; Fr off"} or {@code "24/7"}
     * @return the new schedule
     * @throws InvalidHoursException       if the text cannot be read
     * @throws RestaurantNotFoundException if no restaurant has that id
     */
    public WeeklySchedule updateHours(String restaurantId, String hoursText) {
        Restaurant restaurant = getRestaurant(restaurantId);
        WeeklySchedule schedule = HoursParser.parse(hoursText, HoursSource.MANUAL);
        if (schedule.getSource() == HoursSource.UNKNOWN) {
            throw new InvalidHoursException(hoursText);
        }
        restaurant.changeSchedule(schedule);
        restaurantRepository.save(restaurant);
        return schedule;
    }

    /**
     * Sets where the restaurant is, so customers can find it by distance.
     *
     * @param restaurantId the restaurant to change
     * @param latitude     latitude in degrees
     * @param longitude    longitude in degrees
     * @return the new location
     * @throws IllegalArgumentException    if the coordinates are invalid or too far from Nablus
     * @throws RestaurantNotFoundException if no restaurant has that id
     */
    public Location updateLocation(String restaurantId, double latitude, double longitude) {
        Restaurant restaurant = getRestaurant(restaurantId);
        Location location = new Location(latitude, longitude);
        double distanceKm = location.distanceKmTo(Landmark.OLD_CAMPUS.getLocation());
        if (distanceKm > MAX_KM_FROM_NABLUS) {
            throw new IllegalArgumentException(String.format(Locale.ROOT,
                    "This place is %.1f km from An-Najah; restaurants must be within %.0f km",
                    distanceKm, MAX_KM_FROM_NABLUS));
        }
        restaurant.moveTo(location);
        restaurantRepository.save(restaurant);
        return location;
    }

    /**
     * Adds a new item to the menu. It can be ordered straight away.
     *
     * @param restaurantId the restaurant to change
     * @param name         the item name; extra spaces are removed
     * @param price        the unit price; must be greater than zero
     * @return the new item, with a new id such as {@code manual-63-4}
     * @throws IllegalArgumentException    if the name is empty, already on the menu, or the price is not positive
     * @throws RestaurantNotFoundException if no restaurant has that id
     */
    public MenuItem addMenuItem(String restaurantId, String name, BigDecimal price) {
        Restaurant restaurant = getRestaurant(restaurantId);
        String cleanName = cleanName(name);
        boolean nameTaken = restaurant.getMenuItems().stream()
                .anyMatch(item -> item.getName().equalsIgnoreCase(cleanName));
        if (nameTaken) {
            throw new IllegalArgumentException("'" + cleanName + "' is already on the menu");
        }
        MenuItem item = new MenuItem(nextItemId(restaurant), cleanName, price, true);
        restaurant.addMenuItem(item);
        restaurantRepository.save(restaurant);
        return item;
    }

    /**
     * Changes the price of a menu item.
     *
     * @param restaurantId the restaurant to change
     * @param itemId       the item to change
     * @param newPrice     the new price; must be greater than zero
     * @return the changed item
     * @throws IllegalArgumentException    if the price is not positive
     * @throws MenuItemNotFoundException   if the item is not on the menu
     * @throws RestaurantNotFoundException if no restaurant has that id
     */
    public MenuItem changePrice(String restaurantId, String itemId, BigDecimal newPrice) {
        return changeItem(restaurantId, itemId, item -> item.withPrice(newPrice));
    }

    /**
     * Makes a menu item orderable or not, for example when it runs out for the day.
     *
     * @param restaurantId the restaurant to change
     * @param itemId       the item to change
     * @param available    {@code true} if customers can order it
     * @return the changed item
     * @throws MenuItemNotFoundException   if the item is not on the menu
     * @throws RestaurantNotFoundException if no restaurant has that id
     */
    public MenuItem setAvailable(String restaurantId, String itemId, boolean available) {
        return changeItem(restaurantId, itemId, item -> item.withAvailability(available));
    }

    /**
     * Removes an item from the menu for good.
     *
     * @param restaurantId the restaurant to change
     * @param itemId       the item to remove
     * @throws MenuItemNotFoundException   if the item is not on the menu
     * @throws RestaurantNotFoundException if no restaurant has that id
     */
    public void removeMenuItem(String restaurantId, String itemId) {
        Restaurant restaurant = getRestaurant(restaurantId);
        if (!restaurant.removeMenuItem(itemId)) {
            throw new MenuItemNotFoundException(restaurantId, itemId);
        }
        restaurantRepository.save(restaurant);
    }

    private MenuItem changeItem(String restaurantId, String itemId, UnaryOperator<MenuItem> change) {
        Restaurant restaurant = getRestaurant(restaurantId);
        MenuItem current = restaurant.findMenuItem(itemId)
                .orElseThrow(() -> new MenuItemNotFoundException(restaurantId, itemId));
        MenuItem changed = change.apply(current);
        restaurant.replaceMenuItem(changed);
        restaurantRepository.save(restaurant);
        return changed;
    }

    private static String cleanName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name must not be empty");
        }
        String trimmed = name.trim();
        if (trimmed.contains("|") || trimmed.contains("\n") || trimmed.contains("\r")) {
            throw new IllegalArgumentException("Item name must not contain '|' or line breaks");
        }
        return trimmed;
    }

    /**
     * Picks the next free item id: the restaurant id, a dash and one more than
     * the highest number used so far. Numbers of removed items are not reused
     * while a higher one exists.
     */
    static String nextItemId(Restaurant restaurant) {
        String prefix = restaurant.getId() + "-";
        int highest = restaurant.getMenuItems().stream()
                .map(MenuItem::getId)
                .filter(id -> id.startsWith(prefix))
                .map(id -> id.substring(prefix.length()))
                .filter(RestaurantManagementService::isItemNumber)
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);
        return prefix + (highest + 1);
    }

    private static boolean isItemNumber(String text) {
        return !text.isEmpty() && text.length() <= MAX_ITEM_NUMBER_DIGITS
                && text.chars().allMatch(Character::isDigit);
    }
}
