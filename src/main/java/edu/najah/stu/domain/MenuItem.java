package edu.najah.stu.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Represents a single dish or drink offered on a restaurant's menu.
 *
 * <p>A menu item is immutable: its name, price and availability are fixed
 * when it is created. The price is always stored with exactly two decimal
 * places so that it is displayed and summed correctly.</p>
 */
public class MenuItem {

    /** Number of decimal places used for every price. */
    private static final int PRICE_SCALE = 2;

    /** Unique identifier of the item. */
    private final String id;

    /** Display name of the item, for example "Chicken Shawarma". */
    private final String name;

    /** Price of one unit of the item, always with two decimal places. */
    private final BigDecimal price;

    /** Whether the restaurant can currently serve this item. */
    private final boolean available;

    /**
     * Creates a new menu item.
     *
     * @param id        unique identifier; must not be null or blank
     * @param name      display name; must not be null or blank
     * @param price     unit price; must be greater than zero
     * @param available {@code true} if the item can currently be ordered
     * @throws IllegalArgumentException if any argument is invalid
     */
    public MenuItem(String id, String name, BigDecimal price, boolean available) {
        this.id = requireText(id, "Menu item id");
        this.name = requireText(name, "Menu item name");
        this.price = requirePositive(price);
        this.available = available;
    }

    /**
     * Returns the unique identifier of the item.
     *
     * @return the item id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the display name of the item.
     *
     * @return the item name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the unit price of the item with two decimal places.
     *
     * @return the item price
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Tells whether the item can currently be ordered.
     *
     * @return {@code true} if the item is available
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Checks that a text value is present.
     *
     * @param value     the value to check
     * @param fieldName the field name used in the error message
     * @return the value itself
     * @throws IllegalArgumentException if the value is null or blank
     */
    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }
        return value;
    }

    /**
     * Checks that a price is greater than zero and normalises it to two decimals.
     *
     * @param price the price to check
     * @return the price rounded to two decimal places
     * @throws IllegalArgumentException if the price is null, zero or negative
     */
    private static BigDecimal requirePositive(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Menu item price must be greater than zero");
        }
        return price.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Two menu items are equal when they have the same id.
     *
     * @param other the object to compare with
     * @return {@code true} if both items have the same id
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MenuItem)) {
            return false;
        }
        return id.equals(((MenuItem) other).id);
    }

    /**
     * Returns a hash code based on the item id.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
