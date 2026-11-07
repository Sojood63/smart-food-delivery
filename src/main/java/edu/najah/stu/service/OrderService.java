package edu.najah.stu.service;

import edu.najah.stu.domain.DeliveryAddress;
import edu.najah.stu.domain.Landmark;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Order;
import edu.najah.stu.domain.OrderLine;
import edu.najah.stu.domain.OrderStatus;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.User;
import edu.najah.stu.event.EventPublisher;
import edu.najah.stu.event.OrderEvent;
import edu.najah.stu.repository.OrderRepository;
import edu.najah.stu.repository.RestaurantRepository;
import edu.najah.stu.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Places and cancels orders (Sprint 2).
 *
 * <p>Ordering rules (US2.2): the order is not empty, every item is on the
 * menu and available, and every quantity is valid. Delivery policies (US2.3):
 * the address is in Nablus, the restaurant is within
 * {@value #MAX_DELIVERY_KM} km of it, and the food costs at least
 * {@link #MIN_ORDER_VALUE} ILS. Every new order and every status change is
 * published as an event, so the customer gets an email (Observer).</p>
 */
public class OrderService {

    /** The farthest a restaurant may be from the delivery address. */
    public static final double MAX_DELIVERY_KM = 5;

    /** Addresses farther than this from An-Najah are outside Nablus. */
    public static final double MAX_ADDRESS_KM_FROM_NABLUS = 10;

    /** The smallest food price an order may have, in ILS. */
    public static final BigDecimal MIN_ORDER_VALUE = new BigDecimal("20");

    private final RestaurantRepository restaurants;
    private final OrderRepository orders;
    private final UserRepository users;
    private final TimeProvider timeProvider;
    private final EventPublisher events;

    /**
     * Creates the service. No argument may be null.
     *
     * @param restaurants  where restaurants are stored
     * @param orders       where orders are stored
     * @param users        used to find the manager of a restaurant
     * @param timeProvider the clock
     * @param events       where order events are published
     */
    public OrderService(RestaurantRepository restaurants, OrderRepository orders, UserRepository users,
                        TimeProvider timeProvider, EventPublisher events) {
        if (restaurants == null || orders == null || users == null || timeProvider == null || events == null) {
            throw new IllegalArgumentException("Order service parts must not be null");
        }
        this.restaurants = restaurants;
        this.orders = orders;
        this.users = users;
        this.timeProvider = timeProvider;
        this.events = events;
    }

    /**
     * Places an order (US2.1). The new order is pending and its total is the
     * price of the food.
     *
     * @param customer     the logged-in customer
     * @param restaurantId the restaurant to order from
     * @param quantities   menu item id to quantity, in the order the customer chose them
     * @param address      where to deliver
     * @return the new pending order
     * @throws OrderRejectedException if a rule or a delivery policy is broken
     */
    public Order placeOrder(User customer, String restaurantId, Map<String, Integer> quantities,
                            DeliveryAddress address) {
        if (customer == null || customer.getRole() != Role.CUSTOMER) {
            throw new OrderRejectedException("Only a customer can place an order");
        }
        Restaurant restaurant = restaurants.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
        LocalDateTime now = timeProvider.now();
        if (!restaurant.isOpenAt(now)) {
            throw new OrderRejectedException(restaurant.getName() + " is closed now");
        }
        List<OrderLine> lines = toLines(restaurant, quantities);
        checkDeliveryPolicies(restaurant, address, lines);
        Order order = new Order("O" + (orders.findAll().size() + 1), customer, restaurant, lines, address, now);
        orders.save(order);
        events.publish(OrderEvent.placed(order));
        managerOf(restaurant).ifPresent(manager -> events.publish(OrderEvent.newOrderForRestaurant(manager, order)));
        return order;
    }

    /**
     * Cancels a pending order of the customer (US2.4).
     *
     * @param customer the logged-in customer
     * @param orderId  the order to cancel
     * @return the cancelled order
     * @throws OrderRejectedException if the order is not the customer's
     * @throws IllegalStateException  if preparation has already started
     */
    public Order cancelOrder(User customer, String orderId) {
        Order order = findOrder(orderId);
        if (customer == null || !order.getCustomer().getUsername().equalsIgnoreCase(customer.getUsername())) {
            throw new OrderRejectedException("You can only cancel your own orders");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order " + orderId + " is " + order.getStatus().getDisplayName()
                    + " and can no longer be cancelled");
        }
        order.moveTo(OrderStatus.CANCELLED);
        orders.save(order);
        events.publish(OrderEvent.statusChanged(order));
        return order;
    }

    /**
     * Finds an order.
     *
     * @param orderId the order id
     * @return the order
     * @throws OrderNotFoundException if there is none
     */
    public Order findOrder(String orderId) {
        return orders.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    /**
     * Returns a customer's orders, oldest first.
     *
     * @param customer the customer
     * @return the orders
     */
    public List<Order> ordersOf(User customer) {
        return orders.findAll().stream()
                .filter(order -> order.getCustomer().getUsername().equalsIgnoreCase(customer.getUsername()))
                .toList();
    }

    /**
     * Returns the orders of one restaurant, oldest first.
     *
     * @param restaurantId the restaurant
     * @return the orders
     */
    public List<Order> ordersOfRestaurant(String restaurantId) {
        return orders.findAll().stream()
                .filter(order -> order.getRestaurant().getId().equals(restaurantId))
                .toList();
    }

    private static List<OrderLine> toLines(Restaurant restaurant, Map<String, Integer> quantities) {
        if (quantities == null || quantities.isEmpty()) {
            throw new OrderRejectedException("An order must contain at least one item");
        }
        List<OrderLine> lines = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            MenuItem item = restaurant.findMenuItem(entry.getKey()).orElseThrow(() ->
                    new OrderRejectedException(entry.getKey() + " is not on the menu of " + restaurant.getName()));
            if (!item.isAvailable()) {
                throw new OrderRejectedException(item.getName() + " is not available now");
            }
            try {
                lines.add(new OrderLine(item, entry.getValue() == null ? 0 : entry.getValue()));
            } catch (IllegalArgumentException e) {
                throw new OrderRejectedException(e.getMessage());
            }
        }
        return lines;
    }

    /** Checks US2.3 and returns the delivery distance in km. */
    private static double checkDeliveryPolicies(Restaurant restaurant, DeliveryAddress address,
                                                List<OrderLine> lines) {
        if (address == null) {
            throw new OrderRejectedException("A delivery address is needed");
        }
        if (address.location().distanceKmTo(Landmark.OLD_CAMPUS.getLocation()) > MAX_ADDRESS_KM_FROM_NABLUS) {
            throw new OrderRejectedException("The delivery address is outside Nablus");
        }
        Location restaurantLocation = restaurant.getLocation().orElseThrow(() ->
                new OrderRejectedException(restaurant.getName() + " has no known location, so it cannot deliver"));
        double distanceKm = restaurantLocation.distanceKmTo(address.location());
        if (distanceKm > MAX_DELIVERY_KM) {
            throw new OrderRejectedException(String.format(Locale.ROOT,
                    "%s is %.1f km away; the delivery zone is %.0f km", restaurant.getName(), distanceKm,
                    MAX_DELIVERY_KM));
        }
        if (subtotal(lines).compareTo(MIN_ORDER_VALUE) < 0) {
            throw new OrderRejectedException("The minimum order is " + MIN_ORDER_VALUE.toPlainString() + " ILS");
        }
        return distanceKm;
    }

    private static BigDecimal subtotal(List<OrderLine> lines) {
        return lines.stream().map(OrderLine::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Optional<User> managerOf(Restaurant restaurant) {
        return users.findAll().stream()
                .filter(user -> user.getRestaurantId().filter(restaurant.getId()::equals).isPresent())
                .findFirst();
    }
}
