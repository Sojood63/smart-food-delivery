package edu.najah.stu.domain;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a restaurant in the food delivery system.
 *
 * <p>A restaurant has a unique id, a display name, daily
 * {@link OpeningHours} and a menu of {@link MenuItem}s. The menu can only be changed through
 * {@link #addMenuItem(MenuItem)}, which rejects duplicate items.</p>
 */
public class Restaurant {

    /** Unique identifier of the restaurant. */
    private final String id;

    /** Display name of the restaurant. */
    private final String name;

    /** The daily hours in which the restaurant accepts orders. */
    private final OpeningHours openingHours;

    /** The items offered by this restaurant, available or not. */
    private final List<MenuItem> menuItems = new ArrayList<>();

    /**
     * Creates a restaurant with an empty menu.
     *
     * @param id           unique identifier; must not be null or blank
     * @param name         display name; must not be null or blank
     * @param openingHours daily opening hours; must not be null
     * @throws IllegalArgumentException if the id, name or opening hours are missing
     */
    public Restaurant(String id, String name, OpeningHours openingHours) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Restaurant id must not be empty");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Restaurant name must not be empty");
        }
        if (openingHours == null) {
            throw new IllegalArgumentException("Opening hours must not be null");
        }
        this.id = id;
        this.name = name;
        this.openingHours = openingHours;
    }

    /**
     * Adds an item to this restaurant's menu.
     *
     * @param item the menu item to add; must not be null
     * @throws IllegalArgumentException if the item is null or already on the menu
     */
    public void addMenuItem(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Menu item must not be null");
        }
        if (menuItems.contains(item)) {
            throw new IllegalArgumentException("Menu item already exists: " + item.getId());
        }
        menuItems.add(item);
    }

    /**
     * Returns the restaurant identifier.
     *
     * @return the unique id
     */
    public String getId() {
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
     * Returns the daily opening hours.
     *
     * @return the opening hours
     */
    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    /**
     * Tells whether the restaurant is open at the given time.
     *
     * @param time the time to check; must not be null
     * @return {@code true} if the restaurant is open at that time
     */
    public boolean isOpenAt(LocalTime time) {
        return openingHours.isOpenAt(time);
    }

    /**
     * Returns the menu of this restaurant.
     *
     * @return an unmodifiable copy of all menu items, available or not
     */
    public List<MenuItem> getMenuItems() {
        return List.copyOf(menuItems);
    }
}
