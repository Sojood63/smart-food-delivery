package edu.najah.stu.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Thrown when a login is refused because the account is locked after too
 * many wrong passwords.
 */
public class AccountLockedException extends LoginFailedException {

    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

    private final transient LocalDateTime lockedUntil;

    /**
     * Creates the exception.
     *
     * @param lockedUntil when the user may try again
     */
    public AccountLockedException(LocalDateTime lockedUntil) {
        super("Too many wrong passwords. This account is locked until " + lockedUntil.format(CLOCK));
        this.lockedUntil = lockedUntil;
    }

    /**
     * Returns when the lock ends.
     *
     * @return the end of the lock
     */
    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }
}
