package edu.najah.stu.event;

import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.User;

/**
 * A manager removed an item from the menu for good.
 *
 * @param manager    the manager who made the change
 * @param restaurant the restaurant
 * @param item       the removed item
 */
public record MenuItemRemovedEvent(User manager, Restaurant restaurant, MenuItem item) implements SystemEvent {

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
        return "Menu item removed: " + item.getName();
    }

    @Override
    public String message() {
        return item.getName() + " (" + item.getId() + ") was removed from the menu of " + restaurant.getName()
                + " for good.";
    }
}
