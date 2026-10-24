package edu.najah.stu.domain;

import java.math.BigDecimal;

public class MenuItem {
    private final int id;
    private final String name;
    private final BigDecimal price;
    private boolean available;


    public MenuItem(int id, String name, BigDecimal price, boolean available) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Price must not be negative");
        }
        this.id = id;
        this.name = name;
        this.price = price;
        this.available = available;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}