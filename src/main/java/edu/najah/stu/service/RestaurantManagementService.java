package edu.najah.stu.service;

import edu.najah.stu.domain.HoursParser;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.Landmark;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.User;
import edu.najah.stu.domain.WeeklySchedule;
import edu.najah.stu.event.EventPublisher;
import edu.najah.stu.event.RestaurantEvent;
import edu.najah.stu.repository.RestaurantRepository;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Lets a restaurant's manager change its opening hours, location and menu.
 *
 * <p>Every method takes the logged-in manager and works only on that
 * manager's own restaurant, so one manager can never change another
 * restaurant. Every change is checked first, saved right away, and then
 * published as an event, so the manager gets a confirmation email. A rejected
 * change leaves the restaurant as it was, saves nothing and sends nothing.</p>
 */
public class RestaurantManagementService {

    /** A location further than this from the university is treated as a typing mistake. */
    static final double MAX_KM_FROM_NABLUS = 10;

    private static final int MAX_ITEM_NUMBER_DIGITS = 9;

    private final RestaurantRepository restaurantRepository;
    private final EventPublisher events;

    /**
     * Creates the service.
     *
     * @param restaurantRepository where restaurants are stored; must not be null
     * @param events               where changes are published; must not be null
     */
    public RestaurantManagementService(RestaurantRepository restaurantRepository, EventPublisher events) {
        if (restaurantRepository == null || events == null) {
            throw new IllegalArgumentException("Restaurant repository and events must not be null");
        }
        this.restaurantRepository = restaurantRepository;
        this.events = events;
    }

    /**
     * Finds the restaurant that a manager runs.
     *
     * @param manager the logged-in manager
     * @return the manager's restaurant
     * @throws IllegalArgumentException    if the user is missing or not a manager
     * @throws RestaurantNotFoundException if the manager's restaurant does not exist
     */
    public Restaurant getRestaurant(User manager) {
        if (manager == null || manager.getRole() != Role.MANAGER) {
            throw new IllegalArgumentException("Only a restaurant manager can do this");
        }
        String restaurantId = manager.getRestaurantId().orElseThrow();
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
    }

    /**
     * Sets new opening hours typed by the manager. The hours are marked as
     * coming from the manager, so they win over the map and estimates.
     *
     * @param manager   the logged-in manager
     * @param hoursText the hours, for example {@code "Sa-Th 09:00-23:00; Fr off"} or {@code "24/7"}
     * @return the new schedule
     * @throws InvalidHoursException if the text cannot be read
     */
    public WeeklySchedule updateHours(User manager, String hoursText) {
        Restaurant restaurant = getRestaurant(manager);
        WeeklySchedule schedule = HoursParser.parse(hoursText, HoursSource.MANUAL);
        if (schedule.getSource() == HoursSource.UNKNOWN) {
            throw new InvalidHoursException(hoursText);
        }
        restaurant.changeSchedule(schedule);
        restaurantRepository.save(restaurant);
        events.publish(RestaurantEvent.hoursChanged(manager, restaurant));
        return schedule;
    }

    /**
     * Sets where the restaurant is, so customers can find it by distance.
     *
     * @param manager   the logged-in manager
     * @param latitude  latitude in degrees
     * @param longitude longitude in degrees
     * @return the new location
     * @throws IllegalArgumentException if the coordinates are invalid or too far from Nablus
     */
    public Location updateLocation(User manager, double latitude, double longitude) {
        Restaurant restaurant = getRestaurant(manager);
        Location location = new Location(latitude, longitude);
        double distanceKm = location.distanceKmTo(Landmark.OLD_CAMPUS.getLocation());
        if (distanceKm > MAX_KM_FROM_NABLUS) {
            throw new IllegalArgumentException(String.format(Locale.ROOT,
                    "This place is %.1f km from An-Najah; restaurants must be within %.0f km",
                    distanceKm, MAX_KM_FROM_NABLUS));
        }
        restaurant.moveTo(location);
        restaurantRepository.save(restaurant);
        events.publish(RestaurantEvent.locationChanged(manager, restaurant, location));
        return location;
    }

    /**
     * Adds a new item to the menu. It can be ordered straight away.
     *
     * @param manager the logged-in manager
     * @param name    the item name; extra spaces are removed
     * @param price   the unit price; must be greater than zero
     * @return the new item, with a new id such as {@code manual-63-4}
     * @throws IllegalArgumentException if the name is empty, already on the menu, or the price is not positive
     */
    public MenuItem addMenuItem(User manager, String name, BigDecimal price) {
        Restaurant restaurant = getRestaurant(manager);
        String cleanName = cleanName(name);
        boolean nameTaken = restaurant.getMenuItems().stream()
                .anyMatch(item -> item.getName().equalsIgnoreCase(cleanName));
        if (nameTaken) {
            throw new IllegalArgumentException("'" + cleanName + "' is already on the menu");
        }
        MenuItem item = new MenuItem(nextItemId(restaurant), cleanName, price, true);
        restaurant.addMenuItem(item);
        restaurantRepository.save(restaurant);
        events.publish(RestaurantEvent.itemAdded(manager, restaurant, item));
        return item;
    }

    /**
     * Changes the price of a menu item.
     *
     * @param manager  the logged-in manager
     * @param itemId   the item to change
     * @param newPrice the new price; must be greater than zero
     * @return the changed item
     * @throws IllegalArgumentException  if the price is not positive
     * @throws MenuItemNotFoundException if the item is not on the menu
     */
    public MenuItem changePrice(User manager, String itemId, BigDecimal newPrice) {
        Restaurant restaurant = getRestaurant(manager);
        MenuItem current = findItem(restaurant, itemId);
        MenuItem changed = current.withPrice(newPrice);
        restaurant.replaceMenuItem(changed);
        restaurantRepository.save(restaurant);
        events.publish(RestaurantEvent.priceChanged(manager, restaurant, changed, current.getPrice()));
        return changed;
    }

    /**
     * Makes a menu item orderable or not, for example when it runs out for the day.
     *
     * @param manager   the logged-in manager
     * @param itemId    the item to change
     * @param available {@code true} if customers can order it
     * @return the changed item
     * @throws MenuItemNotFoundException if the item is not on the menu
     */
    public MenuItem setAvailable(User manager, String itemId, boolean available) {
        Restaurant restaurant = getRestaurant(manager);
        MenuItem changed = findItem(restaurant, itemId).withAvailability(available);
        restaurant.replaceMenuItem(changed);
        restaurantRepository.save(restaurant);
        events.publish(RestaurantEvent.availabilityChanged(manager, restaurant, changed));
        return changed;
    }

    /**
     * Removes an item from the menu for good.
     *
     * @param manager the logged-in manager
     * @param itemId  the item to remove
     * @throws MenuItemNotFoundException if the item is not on the menu
     */
    public void removeMenuItem(User manager, String itemId) {
        Restaurant restaurant = getRestaurant(manager);
        MenuItem removed = findItem(restaurant, itemId);
        restaurant.removeMenuItem(itemId);
        restaurantRepository.save(restaurant);
        events.publish(RestaurantEvent.itemRemoved(manager, restaurant, removed));
    }

    private static MenuItem findItem(Restaurant restaurant, String itemId) {
        return restaurant.findMenuItem(itemId)
                .orElseThrow(() -> new MenuItemNotFoundException(restaurant.getId(), itemId));
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
