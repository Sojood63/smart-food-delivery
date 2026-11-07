package edu.najah.stu.domain;

import java.time.LocalDateTime;

/**
 * The kind of order, each with its own rules (US5.1, US5.2).
 * Standard, express and scheduled orders are separate classes.
 */
public interface OrderType {

    /**
     * Returns the name shown to people.
     *
     * @return for example {@code "Express"}
     */
    String name();

    /**
     * Checks the type's own rules when the order is placed.
     *
     * @param now the current time
     * @throws IllegalArgumentException if the order breaks a rule of this type
     */
    void validate(LocalDateTime now);

    /**
     * Tells whether the order is delivered with priority and an extra fee.
     *
     * @return {@code true} for express orders
     */
    default boolean isExpress() {
        return false;
    }
}
