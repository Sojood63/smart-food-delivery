package edu.najah.stu.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * A customer's food order from one restaurant.
 *
 * <p>An order starts as {@link OrderStatus#PENDING} (US2.1). Every change goes
 * through {@link #moveTo}, which rejects steps the order life cycle does not
 * allow, so a delivered or cancelled order can never change again.</p>
 */
public class Order {

    private final String id;
    private final User customer;
    private final Restaurant restaurant;
    private final List<OrderLine> lines;
    private final DeliveryAddress address;
    private final LocalDateTime placedAt;
    private OrderStatus status = OrderStatus.PENDING;
    private Driver driver;

    /**
     * Creates a pending order. The ordering rules are checked by the order
     * service before an order is created.
     *
     * @param id         unique id, for example {@code "O1"}
     * @param customer   who ordered
     * @param restaurant where the food comes from
     * @param lines      the items and quantities; must not be empty
     * @param address    where to deliver
     * @param placedAt   when the order was placed
     * @throws IllegalArgumentException if a value is missing or there are no lines
     */
    public Order(String id, User customer, Restaurant restaurant, List<OrderLine> lines, DeliveryAddress address,
                 LocalDateTime placedAt) {
        if (id == null || customer == null || restaurant == null || address == null || placedAt == null) {
            throw new IllegalArgumentException("Order values must not be null");
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one item");
        }
        this.id = id;
        this.customer = customer;
        this.restaurant = restaurant;
        this.lines = List.copyOf(lines);
        this.address = address;
        this.placedAt = placedAt;
    }

    /**
     * Moves the order to another status.
     *
     * @param next the wanted status
     * @throws IllegalStateException if the life cycle does not allow this step
     */
    public void moveTo(OrderStatus next) {
        if (next == null || !status.canMoveTo(next)) {
            throw new IllegalStateException("Order " + id + " cannot go from " + status.getDisplayName()
                    + " to " + (next == null ? "nothing" : next.getDisplayName()));
        }
        status = next;
    }

    /**
     * Records the driver who delivers this order (US3.1).
     *
     * @param assigned the driver; must not be null
     * @throws IllegalStateException if the order is finished or already has a driver
     */
    public void assignDriver(Driver assigned) {
        if (assigned == null) {
            throw new IllegalArgumentException("Driver must not be null");
        }
        if (status.isFinal() || driver != null) {
            throw new IllegalStateException("Order " + id + " cannot get a driver now");
        }
        driver = assigned;
    }

    /**
     * Returns the number of items, counting quantities.
     *
     * @return for example 3 for two burgers and one drink
     */
    public int itemCount() {
        return lines.stream().mapToInt(OrderLine::quantity).sum();
    }

    /**
     * Returns the price of the food: the sum of all lines.
     *
     * @return the total, with two decimals
     */
    public BigDecimal total() {
        return lines.stream().map(OrderLine::total).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Returns the order id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns who ordered.
     *
     * @return the customer
     */
    public User getCustomer() {
        return customer;
    }

    /**
     * Returns where the food comes from.
     *
     * @return the restaurant
     */
    public Restaurant getRestaurant() {
        return restaurant;
    }

    /**
     * Returns the ordered items.
     *
     * @return an unmodifiable list of lines
     */
    public List<OrderLine> getLines() {
        return lines;
    }

    /**
     * Returns where to deliver.
     *
     * @return the address
     */
    public DeliveryAddress getAddress() {
        return address;
    }

    /**
     * Returns when the order was placed.
     *
     * @return the time
     */
    public LocalDateTime getPlacedAt() {
        return placedAt;
    }

    /**
     * Returns the current step of the order.
     *
     * @return the status
     */
    public OrderStatus getStatus() {
        return status;
    }

    /**
     * Returns the driver, once one is assigned.
     *
     * @return the driver, or empty
     */
    public Optional<Driver> getDriver() {
        return Optional.ofNullable(driver);
    }
}
