package edu.najah.stu.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a restaurant in the food delivery system.
 *
 * <p>A restaurant has a unique id, a display name and a menu of
 * {@link MenuItem}s. The menu can only be changed through
 * {@link #addMenuItem(MenuItem)}, which rejects duplicate items.</p>
 */
public class Restaurant {

    /** Unique identifier of the restaurant. */
    private final String id;

    /** Display name of the restaurant. */
    private final String name;

    /** The items offered by this restaurant, available or not. */
    private final List<MenuItem> menuItems = new ArrayList<>();

    /**
     * Creates a restaurant with an empty menu.
     *
     * @param id   unique identifier; must not be null or blank
     * @param name display name; must not be null or blank
     * @throws IllegalArgumentException if the id or name is missing
     */
    public Restaurant(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Restaurant id must not be empty");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Restaurant name must not be empty");
        }
        this.id = id;
        this.name = name;
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
     * Returns the menu of this restaurant.
     *
     * @return an unmodifiable copy of all menu items, available or not
     */
    public List<MenuItem> getMenuItems() {
        return List.copyOf(menuItems);
    }
}
