package edu.najah.stu.repository;

import edu.najah.stu.domain.Order;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Keeps orders in memory while the program runs.
 */
public class InMemoryOrderRepository implements OrderRepository {

    /** The orders by id, oldest first. */
    private final Map<String, Order> orders = new LinkedHashMap<>();

    @Override
    public void save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order must not be null");
        }
        orders.put(order.getId(), order);
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public List<Order> findAll() {
        return List.copyOf(orders.values());
    }
}
