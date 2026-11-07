package edu.najah.stu.event;

import edu.najah.stu.domain.Driver;
import edu.najah.stu.domain.Order;
import edu.najah.stu.domain.User;

/**
 * Something that happened to an order (US3.3, US3.4). Made with the factory
 * methods, which write the subject and message.
 *
 * @param recipient who is told: the customer, or the restaurant's manager
 * @param order     the order
 * @param subject   the email subject
 * @param message   what happened
 */
public record OrderEvent(User recipient, Order order, String subject, String message) implements SystemEvent {

    private static final String ORDER = "Order ";

    /**
     * The customer placed an order.
     *
     * @param order the new order
     * @return the event for the customer
     */
    public static OrderEvent placed(Order order) {
        return new OrderEvent(order.getCustomer(), order, ORDER + order.getId() + " received",
                "We sent your order to " + order.getRestaurant().getName() + ". Total: "
                        + order.total().toPlainString() + " ILS, paid in cash on delivery.");
    }

    /**
     * A restaurant received a new order.
     *
     * @param manager the restaurant's manager
     * @param order   the new order
     * @return the event for the manager
     */
    public static OrderEvent newOrderForRestaurant(User manager, Order order) {
        return new OrderEvent(manager, order, "New order " + order.getId(),
                order.getCustomer().getUsername() + " ordered " + order.itemCount() + " items. Please confirm it.");
    }

    /**
     * The order moved to a new status.
     *
     * @param order the order, already in its new status
     * @return the event for the customer
     */
    public static OrderEvent statusChanged(Order order) {
        String text = switch (order.getStatus()) {
            case CONFIRMED -> order.getRestaurant().getName() + " accepted your order.";
            case PREPARING -> "Your food is being prepared.";
            case READY -> "Your food is ready and waiting for the driver.";
            case OUT_FOR_DELIVERY -> "Your order is on the way"
                    + order.getDriver().map(driver -> " with " + driver.getName()).orElse("") + ".";
            case DELIVERED -> "Your order was delivered. Enjoy your meal!";
            case CANCELLED -> "Your order was cancelled.";
            default -> "Your order is " + order.getStatus().getDisplayName() + ".";
        };
        return new OrderEvent(order.getCustomer(), order,
                ORDER + order.getId() + ": " + order.getStatus().getDisplayName(), text);
    }

    /**
     * A driver was assigned to the order.
     *
     * @param order the order, with its driver
     * @return the event for the customer
     */
    public static OrderEvent driverAssigned(Order order) {
        String driver = order.getDriver().map(Driver::getName).orElse("A driver");
        return new OrderEvent(order.getCustomer(), order, ORDER + order.getId() + ": driver assigned",
                driver + " will deliver your order.");
    }
}
