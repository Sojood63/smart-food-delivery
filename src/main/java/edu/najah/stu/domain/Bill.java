package edu.najah.stu.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * What the customer pays for an order (US4.2, US4.3).
 *
 * @param subtotal    the price of the food
 * @param deliveryFee the delivery fee after the pricing rules
 * @param discount    money taken off the food price
 * @param pricing     the name of the pricing used, for example {@code "Peak hour"}
 */
public record Bill(BigDecimal subtotal, BigDecimal deliveryFee, BigDecimal discount, String pricing) {

    private static final int CENTS = 2;

    /**
     * Rounds every amount to two decimals.
     *
     * @throws IllegalArgumentException if an amount is missing or negative
     */
    public Bill {
        subtotal = money(subtotal, "Subtotal");
        deliveryFee = money(deliveryFee, "Delivery fee");
        discount = money(discount, "Discount");
        if (pricing == null || pricing.isBlank()) {
            throw new IllegalArgumentException("Pricing name must not be empty");
        }
    }

    private static BigDecimal money(BigDecimal amount, String name) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException(name + " must be zero or more");
        }
        return amount.setScale(CENTS, RoundingMode.HALF_UP);
    }

    /**
     * Returns the final price: food minus discount plus delivery.
     *
     * @return the total to pay
     */
    public BigDecimal total() {
        return subtotal.subtract(discount).add(deliveryFee);
    }
}
