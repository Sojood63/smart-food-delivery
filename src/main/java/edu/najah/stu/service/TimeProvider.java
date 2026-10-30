package edu.najah.stu.service;

import java.time.LocalDateTime;

/**
 * Supplies the current date and time to the system.
 *
 * <p>Services ask this interface for the time instead of calling
 * {@link LocalDateTime#now()} directly, so tests can replace it with a
 * mock that returns any fixed moment.</p>
 */
public interface TimeProvider {

    /**
     * Returns the current date and time.
     *
     * @return the current date and time
     */
    LocalDateTime now();
}