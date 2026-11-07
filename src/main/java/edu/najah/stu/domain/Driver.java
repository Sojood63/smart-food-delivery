package edu.najah.stu.domain;

/**
 * A delivery driver who can be assigned to one order at a time (US3.1).
 */
public class Driver {

    private final String id;
    private final String name;
    private final double speedKmh;
    private Location location;
    private DriverStatus status = DriverStatus.AVAILABLE;

    /**
     * Creates an available driver.
     *
     * @param id       unique id, for example {@code "D1"}
     * @param name     the driver's name
     * @param location where the driver is now
     * @param speedKmh the driver's usual speed in town, in km/h; more than zero
     */
    public Driver(String id, String name, Location location, double speedKmh) {
        if (id == null || id.isBlank() || name == null || name.isBlank() || location == null) {
            throw new IllegalArgumentException("Driver needs an id, a name and a location");
        }
        if (!(speedKmh > 0)) {
            throw new IllegalArgumentException("Speed must be more than zero");
        }
        this.id = id;
        this.name = name;
        this.location = location;
        this.speedKmh = speedKmh;
    }

    /**
     * Returns the usual speed in town.
     *
     * @return the speed in km/h
     */
    public double speedKmh() {
        return speedKmh;
    }

    /**
     * Estimates the minutes to reach the restaurant and then the customer.
     *
     * @param restaurant where the food is picked up
     * @param customer   where it is delivered
     * @return the minutes, rounded up
     */
    public long minutesToDeliver(Location restaurant, Location customer) {
        double km = location.distanceKmTo(restaurant) + restaurant.distanceKmTo(customer);
        return (long) Math.ceil(km / speedKmh() * 60);
    }

    /** Marks the driver as delivering. */
    public void markBusy() {
        status = DriverStatus.BUSY;
    }

    /**
     * Marks the driver as free again, at the place of the last delivery.
     *
     * @param newLocation where the driver is now
     */
    public void markAvailable(Location newLocation) {
        if (newLocation != null) {
            location = newLocation;
        }
        status = DriverStatus.AVAILABLE;
    }

    /**
     * Tells whether the driver can take an order.
     *
     * @return {@code true} if available
     */
    public boolean isAvailable() {
        return status == DriverStatus.AVAILABLE;
    }

    /**
     * Returns the driver id.
     *
     * @return the driver id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the driver's name.
     *
     * @return the driver's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns where the driver is now.
     *
     * @return where the driver is now
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Returns whether the driver is available or busy.
     *
     * @return whether the driver is available or busy
     */
    public DriverStatus getStatus() {
        return status;
    }
}
