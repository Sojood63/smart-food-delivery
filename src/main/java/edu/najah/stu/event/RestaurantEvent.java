package edu.najah.stu.event;

import edu.najah.stu.domain.HoursFormatter;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.User;

import java.math.BigDecimal;

/**
 * A change a manager made to their restaurant. Made with the factory
 * methods, which write the subject and message; the manager is told.
 *
 * @param recipient  the manager who made the change
 * @param restaurant the changed restaurant
 * @param subject    the email subject
 * @param message    what changed
 */
public record RestaurantEvent(User recipient, Restaurant restaurant, String subject, String message)
        implements SystemEvent {

    private static final String CURRENCY = " ILS";

    /**
     * The opening hours changed; the restaurant already has the new hours.
     *
     * @param manager    the manager
     * @param restaurant the restaurant
     * @return the event
     */
    public static RestaurantEvent hoursChanged(User manager, Restaurant restaurant) {
        return new RestaurantEvent(manager, restaurant, "Opening hours updated: " + restaurant.getName(),
                "The opening hours of " + restaurant.getName() + " are now: "
                        + HoursFormatter.format(restaurant.getSchedule())
                        + ".\nCustomers see the new hours right away.");
    }

    /**
     * The restaurant got a new location.
     *
     * @param manager    the manager
     * @param restaurant the restaurant
     * @param location   the new location
     * @return the event
     */
    public static RestaurantEvent locationChanged(User manager, Restaurant restaurant, Location location) {
        return new RestaurantEvent(manager, restaurant, "Location updated: " + restaurant.getName(),
                restaurant.getName() + " is now at " + location.getLatitude() + ", " + location.getLongitude()
                        + ".\nCustomers near this place can now find it by distance.");
    }

    /**
     * An item was added to the menu.
     *
     * @param manager    the manager
     * @param restaurant the restaurant
     * @param item       the new item
     * @return the event
     */
    public static RestaurantEvent itemAdded(User manager, Restaurant restaurant, MenuItem item) {
        return new RestaurantEvent(manager, restaurant, "New menu item: " + item.getName(),
                item.getName() + " (" + item.getId() + ") was added to the menu of " + restaurant.getName()
                        + " for " + item.getPrice().toPlainString() + CURRENCY + ".\nCustomers can order it now.");
    }

    /**
     * The price of an item changed.
     *
     * @param manager    the manager
     * @param restaurant the restaurant
     * @param item       the item with its new price
     * @param oldPrice   the price before the change
     * @return the event
     */
    public static RestaurantEvent priceChanged(User manager, Restaurant restaurant, MenuItem item,
                                               BigDecimal oldPrice) {
        return new RestaurantEvent(manager, restaurant, "Price changed: " + item.getName(),
                "The price of " + item.getName() + " at " + restaurant.getName() + " changed from "
                        + oldPrice.toPlainString() + " to " + item.getPrice().toPlainString() + CURRENCY + ".");
    }

    /**
     * An item became orderable or not.
     *
     * @param manager    the manager
     * @param restaurant the restaurant
     * @param item       the item with its new availability
     * @return the event
     */
    public static RestaurantEvent availabilityChanged(User manager, Restaurant restaurant, MenuItem item) {
        if (item.isAvailable()) {
            return new RestaurantEvent(manager, restaurant, item.getName() + " is available again",
                    "Customers can order " + item.getName() + " from " + restaurant.getName() + " again.");
        }
        return new RestaurantEvent(manager, restaurant, item.getName() + " is now unavailable",
                "Customers cannot order " + item.getName() + " from " + restaurant.getName()
                        + " until you make it available again.");
    }

    /**
     * An item was removed from the menu for good.
     *
     * @param manager    the manager
     * @param restaurant the restaurant
     * @param item       the removed item
     * @return the event
     */
    public static RestaurantEvent itemRemoved(User manager, Restaurant restaurant, MenuItem item) {
        return new RestaurantEvent(manager, restaurant, "Menu item removed: " + item.getName(),
                item.getName() + " (" + item.getId() + ") was removed from the menu of " + restaurant.getName()
                        + " for good.");
    }
}
