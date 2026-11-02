package edu.najah.stu.repository;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * One menu item collected by the team ({@code menus.txt}).
 *
 * @param restaurantId the id of the restaurant in {@code restaurants.txt}
 * @param itemName     the name of the dish or drink
 * @param price        the price in shekels
 * @param available    whether it can be ordered now
 */
public record MenuRow(String restaurantId, String itemName, BigDecimal price, boolean available) {

    /** Number of fields in a line of {@code menus.txt}. */
    public static final int COLUMNS = 4;

    /**
     * Reads a menu item from one line of {@code menus.txt}.
     *
     * @param row the line, split into fields
     * @return the menu row
     * @throws DataFormatException if a field is missing or not valid
     */
    public static MenuRow fromRow(PipeFile.Row row) {
        if (row.field(0).isEmpty() || row.field(1).isEmpty()) {
            throw row.error("restaurant id and item name must not be empty");
        }
        return new MenuRow(row.field(0), row.field(1), price(row), yesOrNo(row, row.field(3)));
    }

    private static BigDecimal price(PipeFile.Row row) {
        BigDecimal price;
        try {
            price = new BigDecimal(row.field(2));
        } catch (NumberFormatException e) {
            throw row.error("invalid price '" + row.field(2) + "'");
        }
        if (price.signum() <= 0) {
            throw row.error("price must be greater than zero");
        }
        return price;
    }

    /**
     * Reads a yes/no field; {@code true}/{@code false} are accepted too.
     */
    static boolean yesOrNo(PipeFile.Row row, String text) {
        return switch (text.toLowerCase(Locale.ROOT)) {
            case "yes", "true" -> true;
            case "no", "false" -> false;
            default -> throw row.error("expected yes or no but found '" + text + "'");
        };
    }
}
