package edu.najah.stu.event;

import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.User;

/**
 * A manager added an item to the menu.
 *
 * @param manager    the manager who made the change
 * @param restaurant the restaurant
 * @param item       the new item
 */
public record MenuItemAddedEvent(User manager, Restaurant restaurant, MenuItem item) implements SystemEvent {

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
        return "New menu item: " + item.getName();
    }

    @Override
    public String message() {
        return item.getName() + " (" + item.getId() + ") was added to the menu of " + restaurant.getName()
                + " for " + item.getPrice().toPlainString() + " ILS.\nCustomers can order it now.";
    }
}
