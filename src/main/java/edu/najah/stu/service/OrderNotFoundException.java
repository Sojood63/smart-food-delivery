package edu.najah.stu.service;

/**
 * Thrown when no order has the requested id.
 */
public class OrderNotFoundException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param orderId the id that was not found
     */
    public OrderNotFoundException(String orderId) {
        super("Order not found: " + orderId);
    }
}
