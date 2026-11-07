package edu.najah.stu.domain;

/**
 * Rides a bicycle: short trips and small orders only.
 */
public class BicycleDriver extends Driver {

    /**
     * Creates a bicycle driver.
     *
     * @param id       unique id
     * @param name     the driver's name
     * @param location where the driver is now
     */
    public BicycleDriver(String id, String name, Location location) {
        super(id, name, location);
    }

    @Override
    public String vehicle() {
        return "Bicycle";
    }

    @Override
    public double speedKmh() {
        return 15;
    }

    @Override
    public double maxDistanceKm() {
        return 3;
    }

    @Override
    public int maxItems() {
        return 5;
    }
}
