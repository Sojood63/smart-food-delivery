package edu.najah.stu.domain;

/**
 * Rides a motorcycle: the fastest in town, for medium distances.
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
        super(id, name, location);
    }

    @Override
    public String vehicle() {
        return "Motorcycle";
    }

    @Override
    public double speedKmh() {
        return 35;
    }

    @Override
    public double maxDistanceKm() {
        return 8;
    }

    @Override
    public int maxItems() {
        return 10;
    }
}
