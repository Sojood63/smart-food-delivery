package edu.najah.stu.delivery;

import edu.najah.stu.domain.Driver;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.Order;
import edu.najah.stu.event.EventPublisher;
import edu.najah.stu.event.OrderEvent;
import edu.najah.stu.repository.DriverRepository;
import edu.najah.stu.repository.OrderRepository;
import edu.najah.stu.service.OrderNotFoundException;

import java.util.List;

/**
 * Assigns a driver to an order (US3.1) with a chosen selection strategy (US3.2).
 *
 * <p>Only available drivers whose vehicle can carry the order over the
 * distance are eligible (US5.4). The chosen driver becomes busy, the order
 * records the driver, and the customer is told (US3.4).</p>
 */
public class DriverAssignmentService {

    private final DriverRepository drivers;
    private final OrderRepository orders;
    private final EventPublisher events;

    /**
     * Creates the service. No argument may be null.
     *
     * @param drivers where drivers are stored
     * @param orders  where orders are stored
     * @param events  where assignments are published
     */
    public DriverAssignmentService(DriverRepository drivers, OrderRepository orders, EventPublisher events) {
        if (drivers == null || orders == null || events == null) {
            throw new IllegalArgumentException("Driver assignment parts must not be null");
        }
        this.drivers = drivers;
        this.orders = orders;
        this.events = events;
    }

    /**
     * Returns the drivers who could take an order now.
     *
     * @param order the order
     * @return available drivers whose vehicle fits the trip and the order size
     */
    public List<Driver> eligibleDrivers(Order order) {
        Location restaurant = order.getRestaurant().getLocation().orElseThrow();
        double tripKm = restaurant.distanceKmTo(order.getAddress().location());
        return drivers.findAll().stream()
                .filter(Driver::isAvailable)
                .filter(driver -> driver.canDeliver(tripKm, order.itemCount()))
                .toList();
    }

    /**
     * Assigns a driver to an order.
     *
     * @param orderId  the order
     * @param strategy how to choose among eligible drivers; must not be null
     * @return the assigned driver
     * @throws NoDriverAvailableException if no driver is eligible
     * @throws IllegalStateException      if the order is finished or already has a driver
     */
    public Driver assign(String orderId, DriverSelectionStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Driver selection strategy must not be null");
        }
        Order order = orders.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        if (order.getStatus().isFinal() || order.getDriver().isPresent()) {
            throw new IllegalStateException("Order " + orderId + " cannot get a driver now");
        }
        Driver driver = strategy.choose(eligibleDrivers(order), order)
                .orElseThrow(() -> new NoDriverAvailableException(orderId));
        order.assignDriver(driver);
        driver.markBusy();
        drivers.save(driver);
        orders.save(order);
        events.publish(OrderEvent.driverAssigned(order));
        return driver;
    }
}
