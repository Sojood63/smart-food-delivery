package edu.najah.stu.presentation;

import edu.najah.stu.delivery.DriverAssignmentService;
import edu.najah.stu.delivery.DriverSelectionStrategy;
import edu.najah.stu.delivery.FastestDriverStrategy;
import edu.najah.stu.delivery.NearestDriverStrategy;
import edu.najah.stu.delivery.NoDriverAvailableException;
import edu.najah.stu.domain.Driver;
import edu.najah.stu.domain.Order;
import edu.najah.stu.domain.OrderStatus;
import edu.najah.stu.domain.PaymentStatus;
import edu.najah.stu.domain.User;
import edu.najah.stu.service.OrderNotFoundException;
import edu.najah.stu.service.OrderService;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * The manager's order screen: move orders through their life cycle (US4.1),
 * assign drivers (US3.1, US3.2) and record cash payments (US4.4).
 */
public class RestaurantOrdersConsole {

    private static final String MENU = String.join(System.lineSeparator(),
            "",
            "1) Show orders",
            "2) Move an order to its next step",
            "3) Assign a driver",
            "4) Record a cash payment",
            "0) Back");

    private final OrderService orderService;
    private final DriverAssignmentService driverService;
    private final ConsoleIO io;

    /**
     * Creates the console. No argument may be null.
     *
     * @param orderService  moves orders and takes payments
     * @param driverService assigns drivers
     * @param in            where answers are read from
     * @param out           where text is printed
     */
    public RestaurantOrdersConsole(OrderService orderService, DriverAssignmentService driverService, Scanner in,
                                   PrintStream out) {
        if (orderService == null || driverService == null) {
            throw new IllegalArgumentException("Order and driver services must not be null");
        }
        this.orderService = orderService;
        this.driverService = driverService;
        this.io = new ConsoleIO(in, out);
    }

    /**
     * Shows the menu for the manager's restaurant until the manager chooses "Back".
     *
     * @param manager the logged-in manager
     */
    public void run(User manager) {
        String restaurantId = manager.getRestaurantId().orElseThrow();
        String choice = io.ask(MENU);
        while (choice != null && !choice.equals("0")) {
            try {
                switch (choice) {
                    case "1" -> showOrders(restaurantId);
                    case "2" -> moveOn(restaurantId);
                    case "3" -> assignDriver(restaurantId);
                    case "4" -> takePayment(restaurantId);
                    default -> io.println("Unknown choice: " + choice);
                }
            } catch (IllegalArgumentException | IllegalStateException | OrderNotFoundException
                     | NoDriverAvailableException e) {
                io.println("Error: " + e.getMessage());
            }
            choice = io.ask(MENU);
        }
    }

    private void showOrders(String restaurantId) {
        List<Order> orders = orderService.ordersOfRestaurant(restaurantId);
        if (orders.isEmpty()) {
            io.println("No orders yet.");
        }
        orders.forEach(order -> io.println(" " + OrderConsole.describe(order)));
    }

    private Order ownOrder(String restaurantId) {
        String orderId = io.answer("Order id:");
        Order order = orderService.findOrder(orderId);
        if (!order.getRestaurant().getId().equals(restaurantId)) {
            throw new OrderNotFoundException(orderId);
        }
        return order;
    }

    private void moveOn(String restaurantId) {
        Order order = ownOrder(restaurantId);
        OrderStatus next = order.getStatus().next().orElseThrow(() ->
                new IllegalStateException("Order " + order.getId() + " is finished"));
        orderService.updateStatus(order.getId(), next);
        io.println("Order " + order.getId() + " is now " + next.getDisplayName());
    }

    private void assignDriver(String restaurantId) {
        Order order = ownOrder(restaurantId);
        String choice = io.answer("Choose by: 1) Nearest driver  2) Fastest driver");
        DriverSelectionStrategy strategy = switch (choice) {
            case "1" -> new NearestDriverStrategy();
            case "2" -> new FastestDriverStrategy();
            default -> throw new IllegalArgumentException("Please choose 1 or 2");
        };
        Driver driver = driverService.assign(order.getId(), strategy);
        io.println(strategy.name() + ": " + driver.getName() + " will deliver order " + order.getId());
    }

    private void takePayment(String restaurantId) {
        Order order = ownOrder(restaurantId);
        io.println("Total: " + order.total().toPlainString() + " ILS");
        BigDecimal amount;
        try {
            amount = new BigDecimal(io.answer("Cash received:").trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please type an amount like 45.50");
        }
        PaymentStatus result = orderService.pay(order.getId(), amount);
        if (result == PaymentStatus.PAID) {
            io.println("Paid. Change: " + amount.subtract(order.total()).toPlainString() + " ILS");
        } else {
            io.println("Payment rejected: the amount is less than the total.");
        }
    }
}
