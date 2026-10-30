package edu.najah.stu.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MenuItemTest {

    @Test
    void createsItemWithCorrectValues() {
        MenuItem item = new MenuItem("M1", "Falafel", new BigDecimal("12.50"), true);

        assertEquals("M1", item.getId());
        assertEquals("Falafel", item.getName());
        assertEquals(new BigDecimal("12.50"), item.getPrice());
        assertTrue(item.isAvailable());
    }

    @Test
    void storesUnavailableFlag() {
        MenuItem item = new MenuItem("M1", "Falafel", new BigDecimal("5"), false);

        assertFalse(item.isAvailable());
    }

    @Test
    void normalisesPriceToTwoDecimals() {
        MenuItem item = new MenuItem("M1", "Falafel", new BigDecimal("12.5"), true);

        assertEquals("12.50", item.getPrice().toPlainString());
    }

    @Test
    void roundsPriceHalfUp() {
        MenuItem item = new MenuItem("M1", "Falafel", new BigDecimal("9.995"), true);

        assertEquals(new BigDecimal("10.00"), item.getPrice());
    }

    @Test
    void rejectsNullPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new MenuItem("M1", "Falafel", null, true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-1", "-0.01"})
    void rejectsZeroOrNegativePrice(String price) {
        assertThrows(IllegalArgumentException.class,
                () -> new MenuItem("M1", "Falafel", new BigDecimal(price), true));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsMissingName(String name) {
        assertThrows(IllegalArgumentException.class,
                () -> new MenuItem("M1", name, BigDecimal.ONE, true));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsMissingId(String id) {
        assertThrows(IllegalArgumentException.class,
                () -> new MenuItem(id, "Falafel", BigDecimal.ONE, true));
    }

    @Test
    void itemsWithSameIdAreEqual() {
        MenuItem first = new MenuItem("M1", "Falafel", BigDecimal.ONE, true);
        MenuItem second = new MenuItem("M1", "Hummus", BigDecimal.TEN, false);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void itemsWithDifferentIdsAreNotEqual() {
        MenuItem first = new MenuItem("M1", "Falafel", BigDecimal.ONE, true);
        MenuItem second = new MenuItem("M2", "Falafel", BigDecimal.ONE, true);

        assertNotEquals(first, second);
    }

    @Test
    void itemEqualsItselfButNotOtherTypes() {
        MenuItem item = new MenuItem("M1", "Falafel", BigDecimal.ONE, true);

        assertEquals(item, item);
        assertNotEquals(item, null);
        assertNotEquals(item, "M1");
    }
}
