package edu.najah.stu.domain;

/**
 * Drives a car: long trips (20 km) and large orders (50 items).
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
        super(id, name, location, 30, 20, 50);
    }

    @Override
    public String vehicle() {
        return "Car";
    }
}
