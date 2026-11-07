package edu.najah.stu.service;

import edu.najah.stu.OrderTestData;
import edu.najah.stu.domain.DeliveryAddress;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.Order;
import edu.najah.stu.domain.OrderStatus;
import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.User;
import edu.najah.stu.event.EventPublisher;
import edu.najah.stu.event.OrderEvent;
import edu.najah.stu.repository.InMemoryOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Placing, rejecting and cancelling orders (Sprint 2).
 * Notifications are a Mockito mock; the clock is fixed.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final String JALTAH = "manual-04";
    private static final String BURGER = "manual-04-1";
    private static final String FRIES = "manual-04-2";

    @Mock
    private EventPublisher events;

    private final User layan = OrderTestData.customer();
    private final User manager = OrderTestData.jaltahManager();
    private final InMemoryOrderRepository orders = new InMemoryOrderRepository();
    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(OrderTestData.restaurants(), orders, OrderTestData.users(layan, manager),
                () -> OrderTestData.MONDAY_4PM, events);
    }

    private static Map<String, Integer> basket(Object... itemAndQuantity) {
        Map<String, Integer> basket = new LinkedHashMap<>();
        for (int i = 0; i < itemAndQuantity.length; i += 2) {
            basket.put((String) itemAndQuantity[i], (Integer) itemAndQuantity[i + 1]);
        }
        return basket;
    }

    private Order placeStandard() {
        return service.placeOrder(layan, JALTAH, basket(BURGER, 2, FRIES, 1), OrderTestData.campusAddress());
    }

    private void rejected(String reason, Runnable placing) {
        OrderRejectedException error = assertThrows(OrderRejectedException.class, placing::run);
        assertTrue(error.getMessage().contains(reason), error.getMessage());
        assertTrue(orders.findAll().isEmpty());
        verify(events, never()).publish(any());
    }

    // ---------- US2.1 place an order ----------

    @Test
    void placedOrderIsSavedPendingWithTheCorrectTotal() {
        Order order = placeStandard();

        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(List.of(order), orders.findAll());
        assertEquals(new BigDecimal("68.00"), order.total());
        assertEquals("O1", order.getId());
        assertEquals(3, order.itemCount());
    }

    @Test
    void customerAndRestaurantAreToldAboutTheNewOrder() {
        Order order = placeStandard();

        verify(events).publish(OrderEvent.placed(order));
        verify(events).publish(OrderEvent.newOrderForRestaurant(manager, order));
    }

    // ---------- US2.2 invalid orders ----------

    @Test
    void unavailableItemIsRejected() {
        rejected("Shake is not available now", () -> service.placeOrder(layan, JALTAH, basket("manual-04-3", 1),
                OrderTestData.campusAddress()));
    }

    @Test
    void itemNotOnTheMenuIsRejected() {
        rejected("manual-99-1 is not on the menu", () -> service.placeOrder(layan, JALTAH, basket("manual-99-1", 1),
                OrderTestData.campusAddress()));
    }

    @Test
    void emptyOrderIsRejected() {
        rejected("at least one item", () -> service.placeOrder(layan, JALTAH, basket(),
                OrderTestData.campusAddress()));
        rejected("at least one item", () -> service.placeOrder(layan, JALTAH, null,
                OrderTestData.campusAddress()));
    }

    @Test
    void invalidQuantitiesAreRejected() {
        rejected("must be from 1 to 20", () -> service.placeOrder(layan, JALTAH, basket(BURGER, 0),
                OrderTestData.campusAddress()));
        rejected("must be from 1 to 20", () -> service.placeOrder(layan, JALTAH, basket(BURGER, 21),
                OrderTestData.campusAddress()));
        rejected("must be from 1 to 20", () -> service.placeOrder(layan, JALTAH, basket(BURGER, null),
                OrderTestData.campusAddress()));
    }

    @Test
    void closedRestaurantAndNonCustomersCannotOrder() {
        rejected("Night Grill is closed now", () -> service.placeOrder(layan, "manual-05", basket("manual-05-1", 1),
                OrderTestData.campusAddress()));
        rejected("Only a customer", () -> service.placeOrder(manager, JALTAH, basket(BURGER, 1),
                OrderTestData.campusAddress()));
        rejected("Only a customer", () -> service.placeOrder(null, JALTAH, basket(BURGER, 1),
                OrderTestData.campusAddress()));
    }

    @Test
    void unknownRestaurantIsReported() {
        Map<String, Integer> items = basket(BURGER, 1);
        DeliveryAddress address = OrderTestData.campusAddress();

        assertThrows(RestaurantNotFoundException.class,
                () -> service.placeOrder(layan, "manual-99", items, address));
    }

    // ---------- US2.3 delivery policies ----------

    @Test
    void restaurantOutsideTheDeliveryZoneIsRejected() {
        DeliveryAddress huwara = new DeliveryAddress("Huwara", new Location(32.1520, 35.2560));

        rejected("the delivery zone is 5 km", () -> service.placeOrder(layan, JALTAH, basket(BURGER, 1), huwara));
    }

    @Test
    void orderBelowTheMinimumIsRejected() {
        rejected("minimum order is 20 ILS", () -> service.placeOrder(layan, JALTAH, basket(FRIES, 1),
                OrderTestData.campusAddress()));
    }

    @Test
    void invalidDeliveryAddressesAreRejected() {
        DeliveryAddress ramallah = new DeliveryAddress("Ramallah", new Location(31.9038, 35.2034));

        rejected("outside Nablus", () -> service.placeOrder(layan, JALTAH, basket(BURGER, 1), ramallah));
        rejected("delivery address is needed", () -> service.placeOrder(layan, JALTAH, basket(BURGER, 1), null));
        rejected("no known location", () -> service.placeOrder(layan, "manual-06", basket("manual-06-1", 1),
                OrderTestData.campusAddress()));
    }

    // ---------- US2.4 cancel ----------

    @Test
    void pendingOrderCanBeCancelled() {
        Order order = placeStandard();

        Order cancelled = service.cancelOrder(layan, order.getId());

        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        verify(events).publish(OrderEvent.statusChanged(order));
    }

    @Test
    void orderInPreparationCannotBeCancelled() {
        Order order = placeStandard();
        order.moveTo(OrderStatus.CONFIRMED);
        order.moveTo(OrderStatus.PREPARING);
        String id = order.getId();

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> service.cancelOrder(layan, id));

        assertTrue(error.getMessage().contains("can no longer be cancelled"));
        assertEquals(OrderStatus.PREPARING, order.getStatus());
    }

    @Test
    void onlyTheOwnerCanCancel() {
        String id = placeStandard().getId();
        User other = new User("ali", "x", Role.CUSTOMER, null, "ali@example.com");

        assertThrows(OrderRejectedException.class, () -> service.cancelOrder(other, id));
        assertThrows(OrderRejectedException.class, () -> service.cancelOrder(null, id));
        assertThrows(OrderNotFoundException.class, () -> service.cancelOrder(layan, "O99"));
    }

    // ---------- lists ----------

    @Test
    void listsOrdersByCustomerAndRestaurant() {
        Order order = placeStandard();

        assertEquals(List.of(order), service.ordersOf(layan));
        assertEquals(List.of(order), service.ordersOfRestaurant(JALTAH));
        assertTrue(service.ordersOfRestaurant("manual-05").isEmpty());
        assertEquals(order, service.findOrder("O1"));
    }

    @Test
    void rejectsMissingParts() {
        TimeProvider clock = () -> OrderTestData.MONDAY_4PM;

        assertThrows(IllegalArgumentException.class, () -> new OrderService(null, orders,
                OrderTestData.users(), clock, events));
        assertThrows(IllegalArgumentException.class, () -> new OrderService(OrderTestData.restaurants(), orders,
                OrderTestData.users(), clock, null));
    }
}
