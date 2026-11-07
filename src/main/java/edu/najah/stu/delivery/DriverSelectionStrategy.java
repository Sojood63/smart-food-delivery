package edu.najah.stu.delivery;

import edu.najah.stu.domain.Driver;
import edu.najah.stu.domain.Order;

import java.util.List;
import java.util.Optional;

/**
 * One way of choosing a driver for an order (Strategy pattern, US3.2).
 */
public interface DriverSelectionStrategy {

    /**
     * Returns the name shown to people.
     *
     * @return for example {@code "Nearest driver"}
     */
    String name();

    /**
     * Chooses one driver from those who are allowed to take the order.
     *
     * @param eligible available drivers whose vehicle fits the order
     * @param order    the order to deliver
     * @return the chosen driver, or empty if the list is empty
     */
    Optional<Driver> choose(List<Driver> eligible, Order order);
}
