package edu.najah.stu.event;

import edu.najah.stu.domain.User;

/**
 * A new customer created an account.
 *
 * @param customer the new customer
 */
public record CustomerRegisteredEvent(User customer) implements SystemEvent {

    @Override
    public String recipientEmail() {
        return customer.getEmail();
    }

    @Override
    public String recipientName() {
        return customer.getUsername();
    }

    @Override
    public String subject() {
        return "Welcome to Smart Food Delivery";
    }

    @Override
    public String message() {
        return "Your customer account is ready. Log in to find restaurants that are open now"
                + " near An-Najah, with their distance and walking time.";
    }
}
