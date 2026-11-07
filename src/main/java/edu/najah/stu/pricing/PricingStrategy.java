package edu.najah.stu.pricing;

import edu.najah.stu.domain.Bill;

import java.math.BigDecimal;

/**
 * One way of pricing an order (Strategy pattern, US4.3).
 * Each strategy decides the delivery fee and any discount.
 */
public interface PricingStrategy {

    /**
     * Makes the bill for an order.
     *
     * @param subtotal    the price of the food
     * @param baseFee     the normal delivery fee for the distance
     * @return the bill
     */
    Bill price(BigDecimal subtotal, BigDecimal baseFee);
}
