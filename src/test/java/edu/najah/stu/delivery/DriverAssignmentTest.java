package edu.najah.stu.delivery;

import edu.najah.stu.OrderTestData;
import edu.najah.stu.domain.BicycleDriver;
import edu.najah.stu.domain.CarDriver;
import edu.najah.stu.domain.DeliveryAddress;
import edu.najah.stu.domain.Driver;
import edu.najah.stu.domain.DriverStatus;
import edu.najah.stu.domain.ElectricVehicleDriver;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.MotorcycleDriver;
import edu.najah.stu.domain.Order;
import edu.najah.stu.domain.OrderStatus;
import edu.najah.stu.domain.StandardOrder;
import edu.najah.stu.event.EventPublisher;
import edu.najah.stu.event.OrderEvent;
import edu.najah.stu.payment.CashPaymentService;
import edu.najah.stu.pricing.PricingService;
import edu.najah.stu.repository.InMemoryDriverRepository;
import edu.najah.stu.repository.InMemoryOrderRepository;
import edu.najah.stu.service.OrderNotFoundException;
import edu.najah.stu.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Assigning drivers (US3.1) with the nearest and fastest strategies (US3.2).
 */
@ExtendWith(MockitoExtension.class)
class DriverAssignmentTest {

    /** The new campus, about 2 km from Jaltah. */
    private static final Location NEW_CAMPUS = new Location(32.227575, 35.220634);

    @Mock
    private EventPublisher events;

    private final InMemoryOrderRepository orders = new InMemoryOrderRepository();
    private final InMemoryDriverRepository drivers = new InMemoryDriverRepository();
    private DriverAssignmentService service;
    private Order order;

    @BeforeEach
    void setUp() {
        OrderService orderService = new OrderService(OrderTestData.restaurants(), orders, OrderTestData.users(),
                new PricingService(() -> OrderTestData.MONDAY_4PM), new CashPaymentService(),
                () -> OrderTestData.MONDAY_4PM, event -> { });
        order = orderService.placeOrder(OrderTestData.customer(), "manual-04", Map.of("manual-04-1", 2),
                new DeliveryAddress("New campus", NEW_CAMPUS), new StandardOrder(), null);
        service = new DriverAssignmentService(drivers, orders, events);
    }

    @Test
    void assignsAnAvailableDriverWhoBecomesBusy() {
        Driver car = new CarDriver("D4", "Yousef", OrderTestData.CAMPUS);
        drivers.save(car);

        Driver assigned = service.assign(order.getId(), new NearestDriverStrategy());

        assertSame(car, assigned);
        assertEquals(DriverStatus.BUSY, car.getStatus());
        assertEquals(Optional.of(car), order.getDriver());
        verify(events).publish(OrderEvent.driverAssigned(order));
    }

    @Test
    void busyAndUnsuitableDriversAreNotEligible() {
        Driver busy = new CarDriver("D1", "Busy", OrderTestData.CAMPUS);
        busy.markBusy();
        Driver lowBattery = new ElectricVehicleDriver("D2", "Low", OrderTestData.CAMPUS, 3);
        Driver farCar = new CarDriver("D3", "Far", new Location(32.25, 35.30));
        drivers.save(busy);
        drivers.save(lowBattery);
        drivers.save(farCar);

        assertEquals(List.of(farCar), service.eligibleDrivers(order));
    }

    @Test
    void nearestStrategyPicksTheClosestDriver() {
        Driver nearBicycle = new BicycleDriver("D1", "Near", OrderTestData.CAMPUS);
        Driver farMotorcycle = new MotorcycleDriver("D2", "Far", new Location(32.2300, 35.2550));
        drivers.save(farMotorcycle);
        drivers.save(nearBicycle);

        assertSame(nearBicycle, service.assign(order.getId(), new NearestDriverStrategy()));
    }

    @Test
    void fastestStrategyPicksTheQuickestDelivery() {
        Driver nearBicycle = new BicycleDriver("D1", "Near", OrderTestData.CAMPUS);
        Driver motorcycle = new MotorcycleDriver("D2", "Quick", new Location(32.2215, 35.2420));
        drivers.save(nearBicycle);
        drivers.save(motorcycle);

        assertSame(motorcycle, service.assign(order.getId(), new FastestDriverStrategy()));
    }

    @Test
    void strategiesHaveNamesAndReturnNothingForNoDrivers() {
        assertEquals("Nearest driver", new NearestDriverStrategy().name());
        assertEquals("Fastest driver", new FastestDriverStrategy().name());
        assertTrue(new NearestDriverStrategy().choose(List.of(), order).isEmpty());
        assertTrue(new FastestDriverStrategy().choose(List.of(), order).isEmpty());
    }

    @Test
    void assignmentIsRejectedWhenNoDriverIsAvailable() {
        String id = order.getId();
        NearestDriverStrategy nearest = new NearestDriverStrategy();

        NoDriverAvailableException error = assertThrows(NoDriverAvailableException.class,
                () -> service.assign(id, nearest));

        assertEquals("No available driver can deliver order O1 right now", error.getMessage());
        assertTrue(order.getDriver().isEmpty());
        verify(events, never()).publish(any());
    }

    @Test
    void finishedOrAlreadyAssignedOrdersCannotGetADriver() {
        drivers.save(new CarDriver("D4", "Yousef", OrderTestData.CAMPUS));
        drivers.save(new CarDriver("D5", "Rami", OrderTestData.CAMPUS));
        String id = order.getId();
        NearestDriverStrategy nearest = new NearestDriverStrategy();
        service.assign(id, nearest);

        assertThrows(IllegalStateException.class, () -> service.assign(id, nearest));
        order.moveTo(OrderStatus.CANCELLED);
        assertThrows(IllegalStateException.class, () -> service.assign(id, nearest));
        assertThrows(OrderNotFoundException.class, () -> service.assign("O99", nearest));
        assertThrows(IllegalArgumentException.class, () -> service.assign(id, null));
    }

    @Test
    void demoDriversHaveOneOfEachVehicle() {
        List<String> vehicles = InMemoryDriverRepository.withDemoDrivers().findAll().stream()
                .map(Driver::vehicle).distinct().toList();

        assertEquals(List.of("Bicycle", "Motorcycle", "Car", "Electric vehicle"), vehicles);
    }

    @Test
    void rejectsMissingParts() {
        assertThrows(IllegalArgumentException.class, () -> new DriverAssignmentService(null, orders, events));
        assertThrows(IllegalArgumentException.class, () -> drivers.save(null));
        assertThrows(IllegalArgumentException.class, () -> orders.save(null));
    }
}
