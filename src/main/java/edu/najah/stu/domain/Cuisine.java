package edu.najah.stu.domain;

/**
 * The main kind of food a restaurant serves, used to filter search results.
 */
public enum Cuisine {
    SWEETS("Sweets"),
    GRILL("Grill"),
    SHAWARMA("Shawarma"),
    PIZZA("Pizza"),
    CHICKEN("Chicken"),
    BURGER("Burger"),
    SEAFOOD("Seafood"),
    CAFE("Cafe"),
    PALESTINIAN("Palestinian"),
    OTHER("Other");

    private final String displayName;

    Cuisine(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the name shown to the customer.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
}
