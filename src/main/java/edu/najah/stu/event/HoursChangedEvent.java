package edu.najah.stu.event;

import edu.najah.stu.domain.HoursFormatter;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.User;

/**
 * A manager changed their restaurant's opening hours.
 *
 * @param manager    the manager who made the change
 * @param restaurant the restaurant, already showing the new hours
 */
public record HoursChangedEvent(User manager, Restaurant restaurant) implements SystemEvent {

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
        return "Opening hours updated: " + restaurant.getName();
    }

    @Override
    public String message() {
        return "The opening hours of " + restaurant.getName() + " are now: "
                + HoursFormatter.format(restaurant.getSchedule()) + ".\nCustomers see the new hours right away.";
    }
}
