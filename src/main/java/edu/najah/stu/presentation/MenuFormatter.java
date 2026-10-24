package edu.najah.stu.presentation;

import edu.najah.stu.domain.MenuItem;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Formats menu data as text, so every view shows prices the same way.
 */
public final class MenuFormatter {

    private static final String CURRENCY = "NIS";

    private MenuFormatter() {
    }

    /**
     * Formats a price with two decimals and the currency.
     *
     * @param price the price to format
     * @return text such as {@code 15.50 NIS}
     */
    public static String formatPrice(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP) + " " + CURRENCY;
    }

    /**
     * Formats one menu line: item name followed by its price.
     *
     * @param item the menu item
     * @return text such as {@code Shawarma   -   15.50 NIS}
     */
    public static String formatMenuLine(MenuItem item) {
        return item.getName() + "   -   " + formatPrice(item.getPrice());
    }

    /**
     * Describes whether an item can be ordered.
     *
     * @param item the menu item
     * @return {@code Available} or {@code Unavailable}
     */
    public static String availabilityText(MenuItem item) {
        return item.isAvailable() ? "Available" : "Unavailable";
    }
}