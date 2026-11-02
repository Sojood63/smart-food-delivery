package edu.najah.stu.domain;

/**
 * A point on the earth given by latitude and longitude in decimal degrees.
 *
 * <p>Distances are measured in a straight line over the earth's surface
 * (the haversine formula), so a real road route is usually a little longer.</p>
 */
public final class Location {

    /** Mean radius of the earth in kilometres. */
    private static final double EARTH_RADIUS_KM = 6371.0;

    private static final double MAX_LATITUDE = 90;
    private static final double MAX_LONGITUDE = 180;

    /** Latitude in degrees, from -90 (south pole) to 90 (north pole). */
    private final double latitude;

    /** Longitude in degrees, from -180 to 180. */
    private final double longitude;

    /**
     * Creates a location.
     *
     * @param latitude  latitude in degrees, between -90 and 90
     * @param longitude longitude in degrees, between -180 and 180
     * @throws IllegalArgumentException if a value is out of range or not a number
     */
    public Location(double latitude, double longitude) {
        if (Double.isNaN(latitude) || Math.abs(latitude) > MAX_LATITUDE) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90: " + latitude);
        }
        if (Double.isNaN(longitude) || Math.abs(longitude) > MAX_LONGITUDE) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180: " + longitude);
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Returns the straight-line distance to another location.
     *
     * @param other the other location; must not be null
     * @return the distance in kilometres
     * @throws IllegalArgumentException if {@code other} is null
     */
    public double distanceKmTo(Location other) {
        if (other == null) {
            throw new IllegalArgumentException("Other location must not be null");
        }
        double deltaLatitude = Math.toRadians(other.latitude - latitude);
        double deltaLongitude = Math.toRadians(other.longitude - longitude);
        double a = Math.pow(Math.sin(deltaLatitude / 2), 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.pow(Math.sin(deltaLongitude / 2), 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(a));
    }

    /**
     * Returns the latitude.
     *
     * @return latitude in degrees
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Returns the longitude.
     *
     * @return longitude in degrees
     */
    public double getLongitude() {
        return longitude;
    }
}
