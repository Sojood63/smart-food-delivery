package edu.najah.stu.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * An order to be delivered later, at a chosen time.
 */
public final class ScheduledOrder implements OrderType {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final LocalDateTime deliverAt;

    /**
     * Creates a scheduled order type.
     *
     * @param deliverAt when the customer wants the food; must not be null
     */
    public ScheduledOrder(LocalDateTime deliverAt) {
        if (deliverAt == null) {
            throw new IllegalArgumentException("A scheduled order needs a delivery time");
        }
        this.deliverAt = deliverAt;
    }

    /**
     * Returns when the food should arrive.
     *
     * @return the delivery time
     */
    public LocalDateTime getDeliverAt() {
        return deliverAt;
    }

    @Override
    public String name() {
        return "Scheduled for " + deliverAt.format(WHEN);
    }

    /**
     * Scheduled orders need a delivery time in the future.
     */
    @Override
    public void validate(LocalDateTime now) {
        if (!deliverAt.isAfter(now)) {
            throw new IllegalArgumentException("A scheduled order needs a future delivery time");
        }
    }
}
