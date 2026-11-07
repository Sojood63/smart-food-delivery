package edu.najah.stu.payment;

import edu.najah.stu.domain.Bill;
import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.DeliveryAddress;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.Landmark;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Order;
import edu.najah.stu.domain.OrderLine;
import edu.najah.stu.domain.PaymentStatus;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.StandardOrder;
import edu.najah.stu.domain.User;
import edu.najah.stu.domain.WeeklySchedule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Cash payment (US4.4).
 */
class CashPaymentServiceTest {

    private final CashPaymentService cash = new CashPaymentService();
    private final Order order = new Order("O1",
            new User("layan", "pbkdf2$1$c2FsdA==$aGFzaA==", Role.CUSTOMER, null, "l@a.co"),
            new Restaurant("r", "R", WeeklySchedule.allDay(HoursSource.MANUAL), null, Cuisine.OTHER),
            List.of(new OrderLine(new MenuItem("r-1", "Burger", new BigDecimal("40"), true), 1)),
            new DeliveryAddress("Campus", Landmark.OLD_CAMPUS.getLocation()), new StandardOrder(),
            new Bill(new BigDecimal("40"), new BigDecimal("5"), BigDecimal.ZERO, "Normal"),
            LocalDateTime.of(2026, 11, 2, 16, 0));

    @Test
    void exactOrMoreCashIsAccepted() {
        assertEquals(PaymentStatus.PAID, cash.pay(order, new BigDecimal("45")));
        assertEquals(PaymentStatus.PAID, cash.pay(order, new BigDecimal("50")));
    }

    @Test
    void tooLittleOrNoCashIsRejected() {
        assertEquals(PaymentStatus.REJECTED, cash.pay(order, new BigDecimal("44.99")));
        assertEquals(PaymentStatus.REJECTED, cash.pay(order, null));
        assertEquals(PaymentStatus.REJECTED, cash.pay(null, BigDecimal.TEN));
    }
}
