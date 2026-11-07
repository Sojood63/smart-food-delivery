package edu.najah.stu.domain;

/**
 * The main kind of food a restaurant serves, used to filter search results.
 */
public enum Cuisine {
    /** Kunafa, cakes and other desserts. */
    SWEETS("Sweets"),

    /** Grilled meat and chicken, such as kebab and shish tawook. */
    GRILL("Grill"),

    /** Shawarma sandwiches and meals. */
    SHAWARMA("Shawarma"),

    /** Pizza and Italian food. */
    PIZZA("Pizza"),

    /** Fried chicken. */
    CHICKEN("Chicken"),

    /** Burgers and sandwiches. */
    BURGER("Burger"),

    /** Fish and seafood. */
    SEAFOOD("Seafood"),

    /** Coffee, drinks and light meals. */
    CAFE("Cafe"),

    /** Traditional Palestinian dishes, such as maqluba and musakhan. */
    PALESTINIAN("Palestinian"),

    /** Anything that fits none of the other kinds; it has no estimated opening hours. */
    OTHER("Other");

    private final String displayName;

    /**
     * Creates a cuisine.
     *
     * @param displayName the name shown to the customer
     */
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
