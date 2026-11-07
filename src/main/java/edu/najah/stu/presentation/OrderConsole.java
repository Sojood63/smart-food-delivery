package edu.najah.stu.presentation;

import edu.najah.stu.domain.Bill;
import edu.najah.stu.domain.DeliveryAddress;
import edu.najah.stu.domain.Landmark;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Order;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.User;
import edu.najah.stu.service.MenuService;
import edu.najah.stu.service.OrderNotFoundException;
import edu.najah.stu.service.OrderRejectedException;
import edu.najah.stu.service.OrderService;
import edu.najah.stu.service.RestaurantService;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/**
 * The customer's ordering screens: place an order (US2.1) and see or cancel
 * their orders (US2.4). Every rule is checked by {@link OrderService}; this
 * class only asks and prints.
 */
public class OrderConsole {

    private static final String ILS = " ILS";

    private final OrderService orderService;
    private final RestaurantService restaurantService;
    private final MenuService menuService;
    private final ConsoleIO io;

    /**
     * Creates the console. No argument may be null.
     *
     * @param orderService      places and cancels orders
     * @param restaurantService finds open restaurants
     * @param menuService       gives the orderable items
     * @param in                where answers are read from
     * @param out               where text is printed
     */
    public OrderConsole(OrderService orderService, RestaurantService restaurantService, MenuService menuService,
                        Scanner in, PrintStream out) {
        if (orderService == null || restaurantService == null || menuService == null) {
            throw new IllegalArgumentException("Order, restaurant and menu services must not be null");
        }
        this.orderService = orderService;
        this.restaurantService = restaurantService;
        this.menuService = menuService;
        this.io = new ConsoleIO(in, out);
    }

    /**
     * Walks the customer through placing an order and prints the bill.
     *
     * @param customer the logged-in customer
     */
    public void placeOrder(User customer) {
        List<Restaurant> open = restaurantService.getOpenRestaurants().stream()
                .filter(restaurant -> restaurant.getLocation().isPresent())
                .filter(restaurant -> !menuService.getAvailableMenu(restaurant.getId()).isEmpty())
                .toList();
        if (open.isEmpty()) {
            io.println("No restaurant can take orders right now.");
            return;
        }
        io.println("Restaurants taking orders now:");
        for (int i = 0; i < open.size(); i++) {
            io.println(" " + (i + 1) + ") " + open.get(i).getName());
        }
        int choice = number(io.answer("Choose a restaurant number:"));
        if (choice < 1 || choice > open.size()) {
            io.println("Error: please choose a number from 1 to " + open.size());
            return;
        }
        Restaurant restaurant = open.get(choice - 1);
        try {
            Map<String, Integer> basket = chooseItems(restaurant);
            DeliveryAddress address = chooseAddress();
            boolean express = io.answer("Express delivery for 10 ILS more? (yes/no):").equalsIgnoreCase("yes");
            String promo = io.answer("Promo code (press Enter for none):");
            Order order = orderService.placeOrder(customer, restaurant.getId(), basket, address, express, promo);
            printBill(order);
        } catch (OrderRejectedException | IllegalArgumentException e) {
            io.println("Error: " + e.getMessage());
        }
    }

    /**
     * Lists the customer's orders and lets them cancel a pending one.
     *
     * @param customer the logged-in customer
     */
    public void myOrders(User customer) {
        List<Order> orders = orderService.ordersOf(customer);
        if (orders.isEmpty()) {
            io.println("You have no orders yet.");
            return;
        }
        orders.forEach(order -> io.println(" " + describe(order)));
        String orderId = io.answer("Order id to cancel (press Enter to go back):");
        if (orderId.isEmpty()) {
            return;
        }
        try {
            Order cancelled = orderService.cancelOrder(customer, orderId);
            io.println("Order " + cancelled.getId() + " is " + cancelled.getStatus().getDisplayName());
        } catch (OrderRejectedException | OrderNotFoundException | IllegalStateException e) {
            io.println("Error: " + e.getMessage());
        }
    }

    /**
     * Describes an order in one line.
     *
     * @param order the order
     * @return for example {@code "O1 | Jaltah | Pending | 45.00 ILS | unpaid"}
     */
    static String describe(Order order) {
        return order.getId() + " | " + order.getRestaurant().getName() + " | " + order.getStatus().getDisplayName()
                + " | " + order.total().toPlainString() + ILS
                + order.getDriver().map(driver -> " | driver " + driver.getName()).orElse("")
                + " | " + order.getPaymentStatus().name().toLowerCase(Locale.ROOT);
    }

    private Map<String, Integer> chooseItems(Restaurant restaurant) {
        List<MenuItem> menu = menuService.getAvailableMenu(restaurant.getId());
        io.println("Menu of " + restaurant.getName() + ":");
        for (int i = 0; i < menu.size(); i++) {
            io.println(" " + (i + 1) + ") " + menu.get(i).getName() + " | " + menu.get(i).getPrice().toPlainString()
                    + ILS);
        }
        Map<String, Integer> basket = new LinkedHashMap<>();
        String answer = io.answer("Item number to add (press Enter when done):");
        while (!answer.isEmpty()) {
            int item = number(answer);
            if (item < 1 || item > menu.size()) {
                io.println("Error: please choose a number from 1 to " + menu.size());
            } else {
                int quantity = number(io.answer("Quantity:"));
                basket.merge(menu.get(item - 1).getId(), quantity, Integer::sum);
            }
            answer = io.answer("Item number to add (press Enter when done):");
        }
        return basket;
    }

    private DeliveryAddress chooseAddress() {
        Landmark[] landmarks = Landmark.values();
        io.println("Deliver to:");
        for (int i = 0; i < landmarks.length; i++) {
            io.println(" " + (i + 1) + ") " + landmarks[i].getDisplayName());
        }
        io.println(" " + (landmarks.length + 1) + ") Another place");
        int choice = number(io.answer("Choose:"));
        if (choice >= 1 && choice <= landmarks.length) {
            Landmark landmark = landmarks[choice - 1];
            return new DeliveryAddress(landmark.getDisplayName(), landmark.getLocation());
        }
        if (choice != landmarks.length + 1) {
            throw new IllegalArgumentException("Invalid delivery address choice");
        }
        String description = io.answer("Address description:");
        double latitude = decimal(io.answer("Latitude:"));
        double longitude = decimal(io.answer("Longitude:"));
        return new DeliveryAddress(description, new Location(latitude, longitude));
    }

    private void printBill(Order order) {
        Bill bill = order.getBill();
        io.println("Order " + order.getId() + " placed: " + order.getStatus().getDisplayName());
        io.println("  Food:         " + bill.subtotal().toPlainString() + ILS);
        if (bill.discount().signum() > 0) {
            io.println("  Discount:    -" + bill.discount().toPlainString() + ILS);
        }
        io.println("  Delivery fee: " + bill.deliveryFee().toPlainString() + ILS + " (" + bill.pricing() + ")");
        io.println("  Total:        " + order.total().toPlainString() + ILS + ", cash on delivery");
    }

    private static int number(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static double decimal(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + text + "' is not a number");
        }
    }
}
