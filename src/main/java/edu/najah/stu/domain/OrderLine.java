package edu.najah.stu.domain;

import java.math.BigDecimal;

/**
 * One menu item and how many of it the customer wants.
 *
 * @param item     the ordered item
 * @param quantity how many, from 1 to {@value #MAX_QUANTITY}
 */
public record OrderLine(MenuItem item, int quantity) {

    /** The most of one item a single order may contain. */
    public static final int MAX_QUANTITY = 20;

    /**
     * Checks the line.
     *
     * @throws IllegalArgumentException if the item is missing or the quantity is out of range
     */
    public OrderLine {
        if (item == null) {
            throw new IllegalArgumentException("Order line needs a menu item");
        }
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity of " + item.getName() + " must be from 1 to "
                    + MAX_QUANTITY + ": " + quantity);
        }
    }

    /**
     * Returns the price of this line: unit price times quantity.
     *
     * @return the line total
     */
    public BigDecimal total() {
        return item.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
