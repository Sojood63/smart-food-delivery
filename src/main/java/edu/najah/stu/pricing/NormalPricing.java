package edu.najah.stu.pricing;

import edu.najah.stu.domain.Bill;

import java.math.BigDecimal;

/**
 * Food price plus the normal delivery fee, no discount.
 */
public class NormalPricing implements PricingStrategy {

    @Override
    public Bill price(BigDecimal subtotal, BigDecimal baseFee) {
        return new Bill(subtotal, baseFee, BigDecimal.ZERO, "Normal");
    }
}
