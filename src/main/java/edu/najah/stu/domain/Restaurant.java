package edu.najah.stu.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a restaurant in the food delivery system.
 * <p>
 * A restaurant has a menu of {@link MenuItem}s, an open/closed status,
 * and delivery settings (minimum order value and delivery radius).
 * </p>
 */
public class Restaurant {

    /** Unique identifier of the restaurant. */
    private final int id;

    /** Display name of the restaurant. */
    private final String name;

    /** Whether the restaurant is currently open for orders. */
    private boolean open;

    /** Minimum order total accepted for delivery. */
    private final BigDecimal minimumOrderValue;

    /** Maximum delivery distance in kilometres. */
    private final double deliveryRadiusKm;

    /** The items offered by this restaurant. */
    private final List<MenuItem> menu = new ArrayList<>();

    /**
     * Creates a restaurant.
     *
     * @param id                unique identifier
     * @param name              display name
     * @param open              whether the restaurant is currently open
     * @param minimumOrderValue minimum order total accepted for delivery
     * @param deliveryRadiusKm  maximum delivery distance in kilometres
     */
    public Restaurant(int id, String name, boolean open,
                      BigDecimal minimumOrderValue, double deliveryRadiusKm) {
        this.id = id;
        this.name = name;
        this.open = open;
        this.minimumOrderValue = minimumOrderValue;
        this.deliveryRadiusKm = deliveryRadiusKm;
    }

    /**
     * Adds an item to this restaurant's menu.
     *
     * @param item the menu item to add
     */
    public void addMenuItem(MenuItem item) {
        menu.add(item);
    }

    /**
     * Returns the menu of this restaurant.
     *
     * @return an unmodifiable list of all menu items (available or not)
     */
    public List<MenuItem> getMenu() {
        return Collections.unmodifiableList(menu);
    }

    /**
     * Returns the restaurant identifier.
     *
     * @return the unique id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the restaurant name.
     *
     * @return the display name
     */
    public String getName() {
        return name;
    }

    /**
     * Tells whether the restaurant is open.
     *
     * @return {@code true} if open, {@code false} if closed
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Opens or closes the restaurant.
     *
     * @param open {@code true} to open, {@code false} to close
     */
    public void setOpen(boolean open) {
        this.open = open;
    }

    /**
     * Returns the minimum order value.
     *
     * @return the minimum order total accepted for delivery
     */
    public BigDecimal getMinimumOrderValue() {
        return minimumOrderValue;
    }

    /**
     * Returns the delivery radius.
     *
     * @return the maximum delivery distance in kilometres
     */
    public double getDeliveryRadiusKm() {
        return deliveryRadiusKm;
    }
}