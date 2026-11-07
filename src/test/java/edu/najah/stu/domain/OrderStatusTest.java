package edu.najah.stu.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The order life cycle (US4.1) and cancelling (US2.4).
 */
class OrderStatusTest {

    @ParameterizedTest
    @CsvSource({"PENDING,CONFIRMED", "CONFIRMED,PREPARING", "PREPARING,READY", "READY,OUT_FOR_DELIVERY",
        "OUT_FOR_DELIVERY,DELIVERED", "PENDING,CANCELLED"})
    void validStepsAreAccepted(OrderStatus from, OrderStatus to) {
        assertTrue(from.canMoveTo(to));
    }

    @ParameterizedTest
    @CsvSource({"PENDING,PREPARING", "PENDING,DELIVERED", "CONFIRMED,PENDING", "PREPARING,CANCELLED",
        "READY,CANCELLED", "DELIVERED,PENDING", "DELIVERED,CANCELLED", "CANCELLED,PENDING", "READY,READY"})
    void invalidStepsAreRejected(OrderStatus from, OrderStatus to) {
        assertFalse(from.canMoveTo(to));
    }

    @Test
    void deliveredAndCancelledAreFinal() {
        assertTrue(OrderStatus.DELIVERED.isFinal());
        assertTrue(OrderStatus.CANCELLED.isFinal());
        assertFalse(OrderStatus.PENDING.isFinal());
    }

    @Test
    void nextFollowsTheNormalPath() {
        assertEquals(Optional.of(OrderStatus.CONFIRMED), OrderStatus.PENDING.next());
        assertEquals(Optional.of(OrderStatus.DELIVERED), OrderStatus.OUT_FOR_DELIVERY.next());
        assertEquals(Optional.empty(), OrderStatus.DELIVERED.next());
        assertEquals("Out for delivery", OrderStatus.OUT_FOR_DELIVERY.getDisplayName());
    }
}
