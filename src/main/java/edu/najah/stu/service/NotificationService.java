package edu.najah.stu.service;

import edu.najah.stu.domain.User;

import java.time.LocalDateTime;

/**
 * Tells users about things that happened to their account.
 *
 * <p>The services only know this interface, so tests can use a mock and
 * the program can send real emails. Sending must never make the action
 * itself fail: a customer is still registered even if the email cannot be
 * sent.</p>
 */
public interface NotificationService {

    /**
     * Welcomes a customer who has just created an account.
     *
     * @param customer the new customer
     */
    void welcome(User customer);

    /**
     * Warns a user that their account was locked after too many wrong passwords.
     *
     * @param user        the locked user
     * @param lockedUntil when they may try again
     */
    void accountLocked(User user, LocalDateTime lockedUntil);
}
