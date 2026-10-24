package edu.najah.stu.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Restaurant {
    private final int id;
    private final String name;
    private boolean open;
    private final BigDecimal minimumOrderValue;
    private final double deliveryRadiusKm;
    private final List<MenuItem> menu = new ArrayList<>();


    public Restaurant(int id, String name, boolean open,
                      BigDecimal minimumOrderValue, double deliveryRadiusKm) {
        this.id = id;
        this.name = name;
        this.open = open;
        this.minimumOrderValue = minimumOrderValue;
        this.deliveryRadiusKm = deliveryRadiusKm;
    }

    public void addMenuItem(MenuItem item) { menu.add(item); }

    public List<MenuItem> getMenu() { return Collections.unmodifiableList(menu); }
    public int getId() { return id; }
    public String getName() { return name; }
    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }
    public BigDecimal getMinimumOrderValue() { return minimumOrderValue; }
    public double getDeliveryRadiusKm() { return deliveryRadiusKm; }
}
