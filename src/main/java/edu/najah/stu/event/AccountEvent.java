package edu.najah.stu.event;

import edu.najah.stu.domain.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Something that happened to a user's account. Made with the factory
 * methods, which write the subject and message.
 *
 * @param recipient the account owner
 * @param subject   the email subject
 * @param message   what happened
 */
public record AccountEvent(User recipient, String subject, String message) implements SystemEvent {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("HH:mm 'on' yyyy-MM-dd");

    /**
     * A new customer created an account.
     *
     * @param customer the new customer
     * @return the event
     */
    public static AccountEvent registered(User customer) {
        return new AccountEvent(customer, "Welcome to Smart Food Delivery",
                "Your customer account is ready. Log in to find restaurants that are open now"
                        + " near An-Najah, with their distance and walking time.");
    }

    /**
     * An account was locked after too many wrong passwords.
     *
     * @param user           the locked user
     * @param wrongPasswords how many wrong passwords in a row caused the lock
     * @param lockedUntil    when the user may try again
     * @return the event
     */
    public static AccountEvent locked(User user, int wrongPasswords, LocalDateTime lockedUntil) {
        return new AccountEvent(user, "Your Smart Food Delivery account was locked",
                "Someone typed a wrong password for your account " + wrongPasswords + " times in a row,"
                        + " so we locked it.\nYou can log in again after " + lockedUntil.format(WHEN) + ".\n"
                        + "If this was not you, choose a stronger password when you can.");
    }
}
