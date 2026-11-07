package edu.najah.stu.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * A customer's food order from one restaurant.
 *
 * <p>An order starts as {@link OrderStatus#PENDING} (US2.1) and then follows
 * the order life cycle (US4.1). Every change goes through {@link #moveTo},
 * which rejects steps that are not allowed, so a delivered or cancelled order
 * can never change again.</p>
 */
public class Order {

    private final String id;
    private final User customer;
    private final Restaurant restaurant;
    private final List<OrderLine> lines;
    private final DeliveryAddress address;
    private final OrderType type;
    private final Bill bill;
    private final LocalDateTime placedAt;
    private OrderStatus status = OrderStatus.PENDING;
    private Driver driver;
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    /**
     * Creates a pending, unpaid order. The checks of the ordering rules are
     * done by the order service before an order is created.
     *
     * @param id         unique id, for example {@code "O1"}
     * @param customer   who ordered
     * @param restaurant where the food comes from
     * @param lines      the items and quantities; must not be empty
     * @param address    where to deliver
     * @param type       standard, express or scheduled
     * @param bill       the prices
     * @param placedAt   when the order was placed
     * @throws IllegalArgumentException if a value is missing or there are no lines
     */
    public Order(String id, User customer, Restaurant restaurant, List<OrderLine> lines, DeliveryAddress address,
                 OrderType type, Bill bill, LocalDateTime placedAt) {
        if (id == null || customer == null || restaurant == null || address == null || type == null
                || bill == null || placedAt == null) {
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
        this.type = type;
        this.bill = bill;
        this.placedAt = placedAt;
    }

    /**
     * Moves the order to its next status.
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
     * Records the driver who delivers this order.
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
     * Records the result of a payment.
     *
     * @param result paid or rejected
     */
    public void recordPayment(PaymentStatus result) {
        if (result == null) {
            throw new IllegalArgumentException("Payment status must not be null");
        }
        paymentStatus = result;
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
     * Returns the final price to pay.
     *
     * @return the bill total
     */
    public BigDecimal total() {
        return bill.total();
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
     * Returns the kind of order.
     *
     * @return standard, express or scheduled
     */
    public OrderType getType() {
        return type;
    }

    /**
     * Returns the prices.
     *
     * @return the bill
     */
    public Bill getBill() {
        return bill;
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

    /**
     * Returns whether the order has been paid.
     *
     * @return the payment status
     */
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
}
