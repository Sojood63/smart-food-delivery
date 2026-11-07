package edu.najah.stu.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Type-specific order rules (US5.1, US5.2).
 */
class OrderTypeTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 2, 16, 0);

    @Test
    void standardOrdersFollowTheNormalRules() {
        OrderType standard = new StandardOrder();

        assertEquals("Standard", standard.name());
        assertFalse(standard.isExpress());
        assertDoesNotThrow(() -> standard.validate(NOW));
    }

    @Test
    void expressOrdersAreMarkedForTheExtraFee() {
        OrderType express = new ExpressOrder();

        assertEquals("Express", express.name());
        assertTrue(express.isExpress());
        assertDoesNotThrow(() -> express.validate(NOW));
    }

    @Test
    void scheduledOrdersNeedAFutureTime() {
        ScheduledOrder later = new ScheduledOrder(NOW.plusHours(3));
        ScheduledOrder now = new ScheduledOrder(NOW);
        ScheduledOrder past = new ScheduledOrder(NOW.minusMinutes(1));

        assertDoesNotThrow(() -> later.validate(NOW));
        assertThrows(IllegalArgumentException.class, () -> now.validate(NOW));
        assertThrows(IllegalArgumentException.class, () -> past.validate(NOW));
        assertEquals("Scheduled for 2026-11-02 19:00", later.name());
        assertEquals(NOW.plusHours(3), later.getDeliverAt());
        assertFalse(later.isExpress());
        assertThrows(IllegalArgumentException.class, () -> new ScheduledOrder(null));
    }
}
