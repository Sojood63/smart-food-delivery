package edu.najah.stu.domain;

/**
 * Rides a motorcycle: the fastest in town, for medium trips (8 km, 10 items).
 */
public class MotorcycleDriver extends Driver {

    /**
     * Creates a motorcycle driver.
     *
     * @param id       unique id
     * @param name     the driver's name
     * @param location where the driver is now
     */
    public MotorcycleDriver(String id, String name, Location location) {
        super(id, name, location, 35, 8, 10);
    }

    @Override
    public String vehicle() {
        return "Motorcycle";
    }
}
