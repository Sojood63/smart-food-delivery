package edu.najah.stu.domain;

/**
 * A delivery driver. Each kind of vehicle is a subclass with its own limits
 * (US5.3, US5.4); the assignment service only calls {@link #canDeliver}.
 */
public abstract class Driver {

    private final String id;
    private final String name;
    private Location location;
    private DriverStatus status = DriverStatus.AVAILABLE;

    /**
     * Creates an available driver.
     *
     * @param id       unique id, for example {@code "D1"}
     * @param name     the driver's name
     * @param location where the driver is now
     */
    protected Driver(String id, String name, Location location) {
        if (id == null || id.isBlank() || name == null || name.isBlank() || location == null) {
            throw new IllegalArgumentException("Driver needs an id, a name and a location");
        }
        this.id = id;
        this.name = name;
        this.location = location;
    }

    /**
     * Returns the kind of vehicle, shown to people.
     *
     * @return for example {@code "Bicycle"}
     */
    public abstract String vehicle();

    /**
     * Returns the usual speed of this vehicle in town.
     *
     * @return the speed in km/h
     */
    public abstract double speedKmh();

    /**
     * Returns the longest trip this vehicle may make, restaurant to customer.
     *
     * @return the distance in km
     */
    public abstract double maxDistanceKm();

    /**
     * Returns the most items this vehicle can carry in one order.
     *
     * @return the number of items
     */
    public abstract int maxItems();

    /**
     * Tells whether this driver's vehicle can carry an order over a distance.
     *
     * @param tripKm     restaurant to customer, in km
     * @param itemCount  items in the order
     * @return {@code true} if the trip and the order fit the vehicle
     */
    public boolean canDeliver(double tripKm, int itemCount) {
        return tripKm <= maxDistanceKm() && itemCount <= maxItems();
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
