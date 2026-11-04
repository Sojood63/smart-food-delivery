package edu.najah.stu.event;

import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.User;

/**
 * A manager made a menu item orderable or not.
 *
 * @param manager    the manager who made the change
 * @param restaurant the restaurant
 * @param item       the item with its new availability
 */
public record MenuItemAvailabilityChangedEvent(User manager, Restaurant restaurant, MenuItem item)
        implements SystemEvent {

    @Override
    public String recipientEmail() {
        return manager.getEmail();
    }

    @Override
    public String recipientName() {
        return manager.getUsername();
    }

    @Override
    public String subject() {
        return item.getName() + (item.isAvailable() ? " is available again" : " is now unavailable");
    }

    @Override
    public String message() {
        return item.isAvailable()
                ? "Customers can order " + item.getName() + " from " + restaurant.getName() + " again."
                : "Customers cannot order " + item.getName() + " from " + restaurant.getName()
                        + " until you make it available again.";
    }
}
