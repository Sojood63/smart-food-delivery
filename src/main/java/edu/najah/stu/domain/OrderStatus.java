package edu.najah.stu.domain;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The steps of an order's life (US4.1):
 * Pending, Confirmed, Preparing, Ready, Out for delivery, Delivered.
 * A pending order can also be cancelled (US2.4).
 */
public enum OrderStatus {

    /** Placed by the customer, waiting for the restaurant. */
    PENDING("Pending"),

    /** Accepted by the restaurant. */
    CONFIRMED("Confirmed"),

    /** Being cooked; it can no longer be cancelled. */
    PREPARING("Preparing"),

    /** Cooked and waiting for the driver. */
    READY("Ready"),

    /** On the way to the customer. */
    OUT_FOR_DELIVERY("Out for delivery"),

    /** Handed to the customer; it can no longer change. */
    DELIVERED("Delivered"),

    /** Cancelled by the customer before preparation. */
    CANCELLED("Cancelled");

    private static final Map<OrderStatus, Set<OrderStatus>> NEXT = Map.of(
            PENDING, Set.of(CONFIRMED, CANCELLED),
            CONFIRMED, Set.of(PREPARING),
            PREPARING, Set.of(READY),
            READY, Set.of(OUT_FOR_DELIVERY),
            OUT_FOR_DELIVERY, Set.of(DELIVERED),
            DELIVERED, Set.of(),
            CANCELLED, Set.of());

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Tells whether an order in this status may move to another one.
     *
     * @param next the wanted status
     * @return {@code true} if the change follows the order life cycle
     */
    public boolean canMoveTo(OrderStatus next) {
        return NEXT.get(this).contains(next);
    }

    /**
     * Returns the next step on the normal path, without cancelling.
     *
     * @return the next status, or empty for a finished order
     */
    public Optional<OrderStatus> next() {
        return NEXT.get(this).stream().filter(status -> status != CANCELLED).findFirst();
    }

    /**
     * Tells whether the order is finished and can no longer change.
     *
     * @return {@code true} for delivered and cancelled orders
     */
    public boolean isFinal() {
        return NEXT.get(this).isEmpty();
    }

    /**
     * Returns the name shown to people.
     *
     * @return the display name, for example {@code "Out for delivery"}
     */
    public String getDisplayName() {
        return displayName;
    }
}
