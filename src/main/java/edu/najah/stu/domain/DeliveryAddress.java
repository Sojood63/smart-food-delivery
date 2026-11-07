package edu.najah.stu.domain;

/**
 * Where an order is delivered: a short description and a point on the map.
 *
 * @param description for example {@code "An-Najah New Campus, gate 2"}
 * @param location    the point on the map
 */
public record DeliveryAddress(String description, Location location) {

    /**
     * Checks the address (US2.3: invalid delivery addresses are rejected).
     *
     * @throws IllegalArgumentException if the description is empty or the location is missing
     */
    public DeliveryAddress {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Delivery address must have a description");
        }
        if (location == null) {
            throw new IllegalArgumentException("Delivery address must have a location");
        }
        description = description.trim();
    }
}
