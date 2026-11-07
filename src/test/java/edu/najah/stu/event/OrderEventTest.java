package edu.najah.stu.event;

import edu.najah.stu.OrderTestData;
import edu.najah.stu.domain.Bill;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.MotorcycleDriver;
import edu.najah.stu.domain.Order;
import edu.najah.stu.domain.OrderLine;
import edu.najah.stu.domain.OrderStatus;
import edu.najah.stu.domain.StandardOrder;
import edu.najah.stu.domain.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Order notifications (US3.3, US3.4).
 */
class OrderEventTest {

    private final User layan = OrderTestData.customer();
    private final Order order = new Order("O7", layan,
            OrderTestData.restaurants().findById("manual-04").orElseThrow(),
            List.of(new OrderLine(new MenuItem("manual-04-1", "Burger", new BigDecimal("28"), true), 2)),
            OrderTestData.campusAddress(), new StandardOrder(),
            new Bill(new BigDecimal("56"), new BigDecimal("5"), BigDecimal.ZERO, "Normal"), OrderTestData.MONDAY_4PM);

    @Test
    void placedOrderTellsTheCustomerTheTotal() {
        OrderEvent event = OrderEvent.placed(order);

        assertSame(layan, event.recipient());
        assertEquals("Order O7 received", event.subject());
        assertTrue(event.message().contains("61.00 ILS"));
    }

    @Test
    void restaurantHearsAboutNewOrders() {
        User manager = OrderTestData.jaltahManager();
        OrderEvent event = OrderEvent.newOrderForRestaurant(manager, order);

        assertSame(manager, event.recipient());
        assertEquals("New order O7", event.subject());
        assertTrue(event.message().startsWith("layan ordered 2 items (Standard)"));
    }

    @Test
    void customerHearsAboutDriverOnTheWayAndDelivery() {
        order.assignDriver(new MotorcycleDriver("D2", "Khaled", OrderTestData.CAMPUS));
        assertEquals("Khaled (Motorcycle) will deliver your order.", OrderEvent.driverAssigned(order).message());

        order.moveTo(OrderStatus.CONFIRMED);
        assertTrue(OrderEvent.statusChanged(order).message().contains("accepted"));
        order.moveTo(OrderStatus.PREPARING);
        assertTrue(OrderEvent.statusChanged(order).message().contains("prepared"));
        order.moveTo(OrderStatus.READY);
        assertTrue(OrderEvent.statusChanged(order).message().contains("ready"));
        order.moveTo(OrderStatus.OUT_FOR_DELIVERY);
        assertEquals("Your order is on the way with Khaled.", OrderEvent.statusChanged(order).message());
        assertEquals("Order O7: Out for delivery", OrderEvent.statusChanged(order).subject());
        order.moveTo(OrderStatus.DELIVERED);
        assertTrue(OrderEvent.statusChanged(order).message().contains("delivered"));
    }

    @Test
    void cancelledAndPendingHaveMessagesToo() {
        assertTrue(OrderEvent.statusChanged(order).message().contains("Pending"));
        assertEquals("A driver will deliver your order.", OrderEvent.driverAssigned(order).message());
        order.moveTo(OrderStatus.CANCELLED);
        assertEquals("Your order was cancelled.", OrderEvent.statusChanged(order).message());
    }
}
