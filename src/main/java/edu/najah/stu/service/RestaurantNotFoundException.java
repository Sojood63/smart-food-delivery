package edu.najah.stu.service;

/**
 * Thrown when no restaurant has the requested id.
 */
public class RestaurantNotFoundException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param restaurantId the id that was not found
     */
    public RestaurantNotFoundException(String restaurantId) {
        super("Restaurant not found: " + restaurantId);
    }
}
