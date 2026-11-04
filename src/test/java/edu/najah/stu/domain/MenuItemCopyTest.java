package edu.najah.stu.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Changing a menu item makes a new item with the same id; the old one stays as it was.
 */
class MenuItemCopyTest {

    private final MenuItem falafel = new MenuItem("R1-1", "Falafel", new BigDecimal("5"), true);

    @Test
    void withPriceKeepsIdNameAndAvailability() {
        MenuItem changed = falafel.withPrice(new BigDecimal("6.5"));

        assertEquals("R1-1", changed.getId());
        assertEquals("Falafel", changed.getName());
        assertEquals(new BigDecimal("6.50"), changed.getPrice());
        assertTrue(changed.isAvailable());
        assertEquals(new BigDecimal("5.00"), falafel.getPrice());
    }

    @Test
    void withPriceRejectsZeroOrNegative() {
        assertThrows(IllegalArgumentException.class, () -> falafel.withPrice(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> falafel.withPrice(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> falafel.withPrice(null));
    }

    @Test
    void withAvailabilityKeepsEverythingElse() {
        MenuItem soldOut = falafel.withAvailability(false);

        assertFalse(soldOut.isAvailable());
        assertEquals(falafel, soldOut);
        assertEquals(falafel.getPrice(), soldOut.getPrice());
        assertTrue(falafel.isAvailable());
    }
}
