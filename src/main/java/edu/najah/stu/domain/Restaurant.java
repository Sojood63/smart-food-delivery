package edu.najah.stu.domain;

import java.util.ArrayList;
import java.util.List;

public class Restaurant {

    private final String id;
    private final String name;
    private final List<MenuItem> menuItems = new ArrayList<>();

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

    public void addMenuItem(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Menu item must not be null");
        }
        if (menuItems.contains(item)) {
            throw new IllegalArgumentException("Menu item already exists: " + item.getId());
        }
        menuItems.add(item);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<MenuItem> getMenuItems() {
        return List.copyOf(menuItems);
    }
}
