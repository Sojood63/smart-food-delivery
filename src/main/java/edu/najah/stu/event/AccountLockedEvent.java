package edu.najah.stu.event;

import edu.najah.stu.domain.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * An account was locked after too many wrong passwords.
 *
 * @param user           the locked user
 * @param wrongPasswords how many wrong passwords in a row caused the lock
 * @param lockedUntil    when the user may try again
 */
public record AccountLockedEvent(User user, int wrongPasswords, LocalDateTime lockedUntil) implements SystemEvent {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("HH:mm 'on' yyyy-MM-dd");

    @Override
    public String recipientEmail() {
        return user.getEmail();
    }

    @Override
    public String recipientName() {
        return user.getUsername();
    }

    @Override
    public String subject() {
        return "Your Smart Food Delivery account was locked";
    }

    @Override
    public String message() {
        return "Someone typed a wrong password for your account " + wrongPasswords + " times in a row,"
                + " so we locked it.\nYou can log in again after " + lockedUntil.format(WHEN) + ".\n"
                + "If this was not you, choose a stronger password when you can.";
    }
}
