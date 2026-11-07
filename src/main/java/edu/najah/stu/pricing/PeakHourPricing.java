package edu.najah.stu.pricing;

import edu.najah.stu.domain.Bill;

import java.math.BigDecimal;

/**
 * At lunch and dinner time the delivery fee is 50% higher.
 */
public class PeakHourPricing implements PricingStrategy {

    /** The delivery fee is multiplied by this at peak hours. */
    public static final BigDecimal FEE_FACTOR = new BigDecimal("1.5");

    @Override
    public Bill price(BigDecimal subtotal, BigDecimal baseFee) {
        return new Bill(subtotal, baseFee.multiply(FEE_FACTOR), BigDecimal.ZERO, "Peak hour");
    }
}
