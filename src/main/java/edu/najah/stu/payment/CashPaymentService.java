package edu.najah.stu.payment;

import edu.najah.stu.domain.Order;
import edu.najah.stu.domain.PaymentStatus;

import java.math.BigDecimal;

/**
 * Cash on delivery: the payment is valid when the driver receives at least the total.
 */
public class CashPaymentService implements PaymentService {

    @Override
    public PaymentStatus pay(Order order, BigDecimal amount) {
        if (order == null || amount == null || amount.compareTo(order.total()) < 0) {
            return PaymentStatus.REJECTED;
        }
        return PaymentStatus.PAID;
    }
}
