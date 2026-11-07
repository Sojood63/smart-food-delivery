package edu.najah.stu.domain;

/**
 * Drives an electric car: like a car, but the battery must have enough range
 * to reach the customer and come back.
 */
public class ElectricVehicleDriver extends Driver {

    private final double rangeKm;

    /**
     * Creates an electric vehicle driver.
     *
     * @param id       unique id
     * @param name     the driver's name
     * @param location where the driver is now
     * @param rangeKm  how far the battery can still go, in km; zero or more
     */
    public ElectricVehicleDriver(String id, String name, Location location, double rangeKm) {
        super(id, name, location, 30, 20, 30);
        if (rangeKm < 0) {
            throw new IllegalArgumentException("Battery range must be zero or more");
        }
        this.rangeKm = rangeKm;
    }

    /**
     * Returns how far the battery can still go.
     *
     * @return the range in km
     */
    public double getRangeKm() {
        return rangeKm;
    }

    @Override
    public String vehicle() {
        return "Electric vehicle";
    }

    /**
     * Also needs battery range for the trip there and back.
     */
    @Override
    public boolean canDeliver(double tripKm, int itemCount) {
        return super.canDeliver(tripKm, itemCount) && tripKm * 2 <= rangeKm;
    }
}
