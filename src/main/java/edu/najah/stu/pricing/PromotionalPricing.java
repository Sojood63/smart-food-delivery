package edu.najah.stu.pricing;

import edu.najah.stu.domain.Bill;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A promotion takes a percentage off the food price; delivery stays normal.
 */
public class PromotionalPricing implements PricingStrategy {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final int percentOff;

    /**
     * Creates a promotion.
     *
     * @param percentOff the discount on the food, from 1 to 100
     */
    public PromotionalPricing(int percentOff) {
        if (percentOff < 1 || percentOff > 100) {
            throw new IllegalArgumentException("Discount must be from 1 to 100 percent: " + percentOff);
        }
        this.percentOff = percentOff;
    }

    @Override
    public Bill price(BigDecimal subtotal, BigDecimal baseFee) {
        BigDecimal discount = subtotal.multiply(BigDecimal.valueOf(percentOff)).divide(HUNDRED, 2, RoundingMode.HALF_UP);
        return new Bill(subtotal, baseFee, discount, "Promotion " + percentOff + "%");
    }
}
