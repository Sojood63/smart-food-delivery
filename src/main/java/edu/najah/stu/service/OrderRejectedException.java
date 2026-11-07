package edu.najah.stu.service;

/**
 * Thrown when an order breaks an ordering rule or a delivery policy (US2.2, US2.3).
 */
public class OrderRejectedException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param reason why the order was rejected, shown to the customer
     */
    public OrderRejectedException(String reason) {
        super(reason);
    }
}
