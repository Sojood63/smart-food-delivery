package edu.najah.stu.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a restaurant in the food delivery system.
 *
 * <p>A restaurant has a unique id, a display name, a
 * {@link WeeklySchedule}, an optional {@link Location}, a {@link Cuisine}
 * and a menu of {@link MenuItem}s. A restaurant without a known location
 * can still be listed as open, but cannot be found by distance. The menu can only be changed through
 * {@link #addMenuItem(MenuItem)}, which rejects duplicate items.</p>
 */
public class Restaurant {

    /** Unique identifier of the restaurant. */
    private final String id;

    /** Display name of the restaurant. */
    private final String name;

    /** The weekly hours in which the restaurant accepts orders. */
    private final WeeklySchedule schedule;

    /** Where the restaurant is; null when not known yet. */
    private final Location location;

    /** The main kind of food the restaurant serves. */
    private final Cuisine cuisine;

    /** The items offered by this restaurant, available or not. */
    private final List<MenuItem> menuItems = new ArrayList<>();

    /**
     * Creates a restaurant with an empty menu.
     *
     * @param id       unique identifier; must not be null or blank
     * @param name     display name; must not be null or blank
     * @param schedule weekly opening hours; must not be null
     * @param location where the restaurant is; null if not known yet
     * @param cuisine  main kind of food served; must not be null
     * @throws IllegalArgumentException if the id, name, schedule or cuisine is missing
     */
    public Restaurant(String id, String name, WeeklySchedule schedule,
                      Location location, Cuisine cuisine) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Restaurant id must not be empty");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Restaurant name must not be empty");
        }
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule must not be null");
        }
        this.id = id;
        this.name = name;
        if (cuisine == null) {
            throw new IllegalArgumentException("Cuisine must not be null");
        }
        this.schedule = schedule;
        this.location = location;
        this.cuisine = cuisine;
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
     * Returns the weekly opening hours.
     *
     * @return the schedule
     */
    public WeeklySchedule getSchedule() {
        return schedule;
    }

    /**
     * Returns where the restaurant is.
     *
     * @return the location, or empty if it is not known yet
     */
    public Optional<Location> getLocation() {
        return Optional.ofNullable(location);
    }

    /**
     * Returns the main kind of food served.
     *
     * @return the cuisine
     */
    public Cuisine getCuisine() {
        return cuisine;
    }

    /**
     * Tells whether the restaurant is open at the given moment.
     *
     * @param moment the date and time to check; must not be null
     * @return {@code true} if the restaurant is open at that moment
     */
    public boolean isOpenAt(LocalDateTime moment) {
        return schedule.isOpenAt(moment);
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
