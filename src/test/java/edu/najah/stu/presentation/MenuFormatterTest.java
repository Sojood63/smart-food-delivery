package edu.najah.stu.presentation;

import edu.najah.stu.domain.MenuItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MenuFormatterTest {

    @Test
    void formatPriceShowsTwoDecimalsAndCurrency() {
        assertEquals("15.50 NIS", MenuFormatter.formatPrice(new BigDecimal("15.5")));
    }

    @Test
    void formatPriceAddsDecimalsToWholeNumbers() {
        assertEquals("8.00 NIS", MenuFormatter.formatPrice(new BigDecimal("8")));
    }

    @Test
    void formatPriceRoundsExtraDecimals() {
        assertEquals("10.01 NIS", MenuFormatter.formatPrice(new BigDecimal("10.005")));
    }

    @Test
    void formatMenuLineContainsNameAndPrice() {
        MenuItem item = new MenuItem(1, "Shawarma", new BigDecimal("15.50"), true);

        assertEquals("Shawarma   -   15.50 NIS", MenuFormatter.formatMenuLine(item));
    }

    @Test
    void availabilityTextForAvailableItem() {
        MenuItem item = new MenuItem(1, "Shawarma", new BigDecimal("15.50"), true);

        assertEquals("Available", MenuFormatter.availabilityText(item));
    }

    @Test
    void availabilityTextForUnavailableItem() {
        MenuItem item = new MenuItem(2, "Falafel", new BigDecimal("8"), false);

        assertEquals("Unavailable", MenuFormatter.availabilityText(item));
    }
}