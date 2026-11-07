package edu.najah.stu.domain;

/**
 * Rides a bicycle: slow, short trips (3 km) and small orders (5 items) only.
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
        super(id, name, location, 15, 3, 5);
    }

    @Override
    public String vehicle() {
        return "Bicycle";
    }
}
