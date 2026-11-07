package edu.najah.stu.delivery;

/**
 * Thrown when no available driver can take an order (US3.2).
 */
public class NoDriverAvailableException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param orderId the order that has no driver
     */
    public NoDriverAvailableException(String orderId) {
        super("No available driver can deliver order " + orderId + " right now");
    }
}
