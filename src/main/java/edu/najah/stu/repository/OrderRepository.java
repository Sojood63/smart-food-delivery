package edu.najah.stu.repository;

import edu.najah.stu.domain.Order;

import java.util.List;
import java.util.Optional;

/**
 * Stores and finds orders.
 */
public interface OrderRepository {

    /**
     * Saves an order, replacing any order with the same id.
     *
     * @param order the order; must not be null
     */
    void save(Order order);

    /**
     * Finds an order by id.
     *
     * @param id the order id
     * @return the order, or empty
     */
    Optional<Order> findById(String id);

    /**
     * Returns all orders, oldest first.
     *
     * @return an unmodifiable list
     */
    List<Order> findAll();
}
