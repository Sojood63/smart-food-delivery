package edu.najah.stu.domain;

/**
 * What a user is allowed to do in the system.
 */
public enum Role {

    /** Browses restaurants and orders food. */
    CUSTOMER("Customer"),

    /** Runs one restaurant: its hours, location and menu. */
    MANAGER("Manager");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the name shown to people.
     *
     * @return the display name, for example {@code "Manager"}
     */
    public String getDisplayName() {
        return displayName;
    }
}
