package edu.najah.stu.domain;

/**
 * Well-known places a customer can search from instead of typing coordinates.
 */
public enum Landmark {

    /** An-Najah National University, old campus. */
    OLD_CAMPUS("An-Najah Old Campus", new Location(32.2189, 35.2400)),

    /** An-Najah National University, new campus. */
    NEW_CAMPUS("An-Najah New Campus", new Location(32.227575, 35.220634));

    private final String displayName;
    private final Location location;

    Landmark(String displayName, Location location) {
        this.displayName = displayName;
        this.location = location;
    }

    /**
     * Returns the name shown to the customer.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns where the landmark is.
     *
     * @return the landmark location
     */
    public Location getLocation() {
        return location;
    }
}
