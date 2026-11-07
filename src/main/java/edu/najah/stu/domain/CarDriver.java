package edu.najah.stu.domain;

/**
 * Drives a car: long trips and large orders.
 */
public class CarDriver extends Driver {

    /**
     * Creates a car driver.
     *
     * @param id       unique id
     * @param name     the driver's name
     * @param location where the driver is now
     */
    public CarDriver(String id, String name, Location location) {
        super(id, name, location);
    }

    @Override
    public String vehicle() {
        return "Car";
    }

    @Override
    public double speedKmh() {
        return 30;
    }

    @Override
    public double maxDistanceKm() {
        return 20;
    }

    @Override
    public int maxItems() {
        return 50;
    }
}
