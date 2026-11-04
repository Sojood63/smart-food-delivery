package edu.najah.stu.event;

import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.User;

import java.math.BigDecimal;

/**
 * A manager changed the price of a menu item.
 *
 * @param manager    the manager who made the change
 * @param restaurant the restaurant
 * @param item       the item with its new price
 * @param oldPrice   the price before the change
 */
public record MenuItemPriceChangedEvent(User manager, Restaurant restaurant, MenuItem item, BigDecimal oldPrice)
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
        return "Price changed: " + item.getName();
    }

    @Override
    public String message() {
        return "The price of " + item.getName() + " at " + restaurant.getName() + " changed from "
                + oldPrice.toPlainString() + " to " + item.getPrice().toPlainString() + " ILS.";
    }
}
