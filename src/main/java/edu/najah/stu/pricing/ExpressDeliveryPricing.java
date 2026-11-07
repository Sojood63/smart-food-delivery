package edu.najah.stu.pricing;

import edu.najah.stu.domain.Bill;

import java.math.BigDecimal;

/**
 * Express delivery pays an extra fee on top of the normal delivery fee.
 */
public class ExpressDeliveryPricing implements PricingStrategy {

    /** The extra fee for express delivery, in ILS. */
    public static final BigDecimal EXPRESS_FEE = new BigDecimal("10");

    @Override
    public Bill price(BigDecimal subtotal, BigDecimal baseFee) {
        return new Bill(subtotal, baseFee.add(EXPRESS_FEE), BigDecimal.ZERO, "Express");
    }
}
