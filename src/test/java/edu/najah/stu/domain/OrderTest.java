package edu.najah.stu.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Orders, order lines and addresses.
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
        order = new Order("O1", layan, jaltah, List.of(new OrderLine(burger, 2)), address, NOW);
    }

    @Test
    void newOrderIsPendingWithTheFoodTotal() {
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(new BigDecimal("56.00"), order.total());
        assertEquals(2, order.itemCount());
        assertEquals(NOW, order.getPlacedAt());
    }

    @Test
    void cancelledOrderCannotChangeAgain() {
        order.moveTo(OrderStatus.CANCELLED);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> order.moveTo(OrderStatus.CONFIRMED));
        assertEquals("Order O1 cannot go from Cancelled to Confirmed", error.getMessage());
        assertThrows(IllegalStateException.class, () -> order.moveTo(null));
    }

    @Test
    void orderNeedsLinesAndValues() {
        List<OrderLine> none = List.of();

        assertThrows(IllegalArgumentException.class, () -> new Order("O2", layan, jaltah, none, address, NOW));
        assertThrows(IllegalArgumentException.class, () -> new Order("O2", layan, jaltah, null, address, NOW));
        assertThrows(IllegalArgumentException.class,
                () -> new Order(null, layan, jaltah, order.getLines(), address, NOW));
    }

    @Test
    void gettersReturnWhatWasGiven() {
        assertEquals("O1", order.getId());
        assertEquals(layan, order.getCustomer());
        assertEquals(jaltah, order.getRestaurant());
        assertEquals(address, order.getAddress());
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
}
