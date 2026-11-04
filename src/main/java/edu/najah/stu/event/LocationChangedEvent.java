package edu.najah.stu.event;

import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.User;

/**
 * A manager set where their restaurant is.
 *
 * @param manager    the manager who made the change
 * @param restaurant the restaurant
 * @param location   the new location
 */
public record LocationChangedEvent(User manager, Restaurant restaurant, Location location) implements SystemEvent {

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
        return "Location updated: " + restaurant.getName();
    }

    @Override
    public String message() {
        return restaurant.getName() + " is now at " + location.getLatitude() + ", " + location.getLongitude()
                + ".\nCustomers near this place can now find it by distance.";
    }
}
