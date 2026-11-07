package edu.najah.stu.event;

import edu.najah.stu.OrderTestData;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Order;
import edu.najah.stu.domain.OrderLine;
import edu.najah.stu.domain.OrderStatus;
import edu.najah.stu.domain.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Order notifications.
 */
class OrderEventTest {

    private final User layan = OrderTestData.customer();
    private final Order order = new Order("O7", layan,
            OrderTestData.restaurants().findById("manual-04").orElseThrow(),
            List.of(new OrderLine(new MenuItem("manual-04-1", "Burger", new BigDecimal("28"), true), 2)),
            OrderTestData.campusAddress(), OrderTestData.MONDAY_4PM);

    @Test
    void placedOrderTellsTheCustomerTheTotal() {
        OrderEvent event = OrderEvent.placed(order);

        assertSame(layan, event.recipient());
        assertEquals("Order O7 received", event.subject());
        assertTrue(event.message().contains("56.00 ILS"));
    }

    @Test
    void restaurantHearsAboutNewOrders() {
        User manager = OrderTestData.jaltahManager();
        OrderEvent event = OrderEvent.newOrderForRestaurant(manager, order);

        assertSame(manager, event.recipient());
        assertEquals("New order O7", event.subject());
        assertTrue(event.message().startsWith("layan ordered 2 items"));
    }

    @Test
    void statusChangesAreDescribed() {
        assertEquals("Your order is Pending.", OrderEvent.statusChanged(order).message());
        order.moveTo(OrderStatus.CANCELLED);
        assertEquals("Your order was cancelled.", OrderEvent.statusChanged(order).message());
        assertEquals("Order O7: Cancelled", OrderEvent.statusChanged(order).subject());
    }
}
