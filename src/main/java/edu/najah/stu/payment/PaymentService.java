package edu.najah.stu.payment;

import edu.najah.stu.domain.Order;
import edu.najah.stu.domain.PaymentStatus;

import java.math.BigDecimal;

/**
 * Takes payment for an order (US4.4). Tests use a Mockito mock of this interface.
 */
public interface PaymentService {

    /**
     * Tries to take payment for an order.
     *
     * @param order  the order to pay
     * @param amount the money given
     * @return {@link PaymentStatus#PAID} or {@link PaymentStatus#REJECTED}
     */
    PaymentStatus pay(Order order, BigDecimal amount);
}
