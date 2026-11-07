package edu.najah.stu.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Orders, order lines, addresses and bills.
 */
class OrderTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 2, 16, 0);
    private static final Location CAMPUS = Landmark.OLD_CAMPUS.getLocation();

    private final User layan = new User("layan", "pbkdf2$1$c2FsdA==$aGFzaA==", Role.CUSTOMER, null, "l@a.co");
    private final Restaurant jaltah = new Restaurant("manual-04", "Jaltah", WeeklySchedule.allDay(HoursSource.MANUAL),
            CAMPUS, Cuisine.BURGER);
    private final MenuItem burger = new MenuItem("manual-04-1", "Burger", new BigDecimal("28"), true);
    private final DeliveryAddress address = new DeliveryAddress("Old campus", CAMPUS);
    private Order order;

    @BeforeEach
    void setUp() {
        Bill bill = new Bill(new BigDecimal("56"), new BigDecimal("5"), BigDecimal.ZERO, "Normal");
        order = new Order("O1", layan, jaltah, List.of(new OrderLine(burger, 2)), address, new StandardOrder(),
                bill, NOW);
    }

    @Test
    void newOrderIsPendingAndUnpaid() {
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(PaymentStatus.UNPAID, order.getPaymentStatus());
        assertEquals(Optional.empty(), order.getDriver());
        assertEquals(new BigDecimal("61.00"), order.total());
        assertEquals(2, order.itemCount());
        assertEquals(NOW, order.getPlacedAt());
    }

    @Test
    void followsTheLifeCycle() {
        for (OrderStatus next : List.of(OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.READY,
                OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED)) {
            order.moveTo(next);
        }
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void invalidStepIsRejectedAndNothingChanges() {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> order.moveTo(OrderStatus.DELIVERED));

        assertEquals("Order O1 cannot go from Pending to Delivered", error.getMessage());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertThrows(IllegalStateException.class, () -> order.moveTo(null));
    }

    @Test
    void deliveredOrderCannotBeModified() {
        order.moveTo(OrderStatus.CONFIRMED);
        order.moveTo(OrderStatus.PREPARING);
        order.moveTo(OrderStatus.READY);
        order.moveTo(OrderStatus.OUT_FOR_DELIVERY);
        order.moveTo(OrderStatus.DELIVERED);
        Driver driver = new CarDriver("D1", "Yousef", CAMPUS);

        assertThrows(IllegalStateException.class, () -> order.moveTo(OrderStatus.CANCELLED));
        assertThrows(IllegalStateException.class, () -> order.assignDriver(driver));
    }

    @Test
    void recordsOneDriverOnly() {
        Driver first = new CarDriver("D1", "Yousef", CAMPUS);
        Driver second = new CarDriver("D2", "Rami", CAMPUS);

        order.assignDriver(first);

        assertEquals(Optional.of(first), order.getDriver());
        assertThrows(IllegalStateException.class, () -> order.assignDriver(second));
        assertThrows(IllegalArgumentException.class, () -> order.assignDriver(null));
    }

    @Test
    void recordsPayment() {
        order.recordPayment(PaymentStatus.PAID);

        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        assertThrows(IllegalArgumentException.class, () -> order.recordPayment(null));
    }

    @Test
    void orderNeedsLinesAndValues() {
        Bill bill = order.getBill();
        List<OrderLine> none = List.of();
        StandardOrder type = new StandardOrder();

        assertThrows(IllegalArgumentException.class,
                () -> new Order("O2", layan, jaltah, none, address, type, bill, NOW));
        assertThrows(IllegalArgumentException.class,
                () -> new Order("O2", layan, jaltah, null, address, type, bill, NOW));
        assertThrows(IllegalArgumentException.class,
                () -> new Order(null, layan, jaltah, order.getLines(), address, type, bill, NOW));
    }

    @Test
    void gettersReturnWhatWasGiven() {
        assertEquals("O1", order.getId());
        assertEquals(layan, order.getCustomer());
        assertEquals(jaltah, order.getRestaurant());
        assertEquals(address, order.getAddress());
        assertEquals("Standard", order.getType().name());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 21})
    void invalidQuantitiesAreRejected(int quantity) {
        assertThrows(IllegalArgumentException.class, () -> new OrderLine(burger, quantity));
    }

    @Test
    void lineTotalIsPriceTimesQuantity() {
        assertEquals(new BigDecimal("84.00"), new OrderLine(burger, 3).total());
        assertThrows(IllegalArgumentException.class, () -> new OrderLine(null, 1));
    }

    @Test
    void invalidAddressesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new DeliveryAddress(" ", CAMPUS));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryAddress(null, CAMPUS));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryAddress("Home", null));
        assertEquals("Home", new DeliveryAddress("  Home ", CAMPUS).description());
    }

    @Test
    void billRoundsAndAddsUp() {
        Bill bill = new Bill(new BigDecimal("50"), new BigDecimal("7.5"), new BigDecimal("5"), "Promotion 10%");

        assertEquals(new BigDecimal("52.50"), bill.total());
        BigDecimal negative = new BigDecimal("-1");
        assertThrows(IllegalArgumentException.class, () -> new Bill(negative, BigDecimal.ONE, BigDecimal.ZERO, "x"));
        assertThrows(IllegalArgumentException.class, () -> new Bill(null, BigDecimal.ONE, BigDecimal.ZERO, "x"));
        assertThrows(IllegalArgumentException.class, () -> new Bill(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO,
                " "));
    }
}
