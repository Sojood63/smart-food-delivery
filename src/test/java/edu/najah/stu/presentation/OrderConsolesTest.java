package edu.najah.stu.presentation;

import edu.najah.stu.OrderTestData;
import edu.najah.stu.delivery.DriverAssignmentService;
import edu.najah.stu.domain.Driver;
import edu.najah.stu.domain.Order;
import edu.najah.stu.domain.OrderStatus;
import edu.najah.stu.domain.PaymentStatus;
import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.User;
import edu.najah.stu.payment.CashPaymentService;
import edu.najah.stu.pricing.PricingService;
import edu.najah.stu.repository.InMemoryDriverRepository;
import edu.najah.stu.repository.InMemoryOrderRepository;
import edu.najah.stu.repository.InMemoryRestaurantRepository;
import edu.najah.stu.service.MenuService;
import edu.najah.stu.service.OrderService;
import edu.najah.stu.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the customer's ordering screens and the manager's order screen with typed answers.
 */
class OrderConsolesTest {

    private final User layan = OrderTestData.customer();
    private final User sojood = OrderTestData.jaltahManager();
    private InMemoryRestaurantRepository restaurants;
    private InMemoryDriverRepository drivers;
    private InMemoryOrderRepository orders;
    private OrderService orderService;
    private String printed;

    @BeforeEach
    void setUp() {
        restaurants = OrderTestData.restaurants();
        drivers = new InMemoryDriverRepository();
        orders = new InMemoryOrderRepository();
        orderService = new OrderService(restaurants, orders, OrderTestData.users(layan, sojood),
                new PricingService(() -> OrderTestData.MONDAY_4PM), new CashPaymentService(),
                () -> OrderTestData.MONDAY_4PM, event -> { });
    }

    private OrderConsole customerScreen(String... lines) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Scanner in = new Scanner(String.join("\n", lines) + "\n");
        RestaurantService restaurantService = new RestaurantService(restaurants, () -> OrderTestData.MONDAY_4PM);
        return new OrderConsole(orderService, restaurantService, new MenuService(restaurants), in,
                new PrintStream(output, true, StandardCharsets.UTF_8)) {
            @Override
            public void placeOrder(User customer) {
                super.placeOrder(customer);
                printed = output.toString(StandardCharsets.UTF_8);
            }

            @Override
            public void myOrders(User customer) {
                super.myOrders(customer);
                printed = output.toString(StandardCharsets.UTF_8);
            }
        };
    }

    private void managerScreen(String... lines) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new RestaurantOrdersConsole(orderService, assignment(), new Scanner(String.join("\n", lines) + "\n"),
                new PrintStream(output, true, StandardCharsets.UTF_8)).run(sojood);
        printed = output.toString(StandardCharsets.UTF_8);
    }

    private DriverAssignmentService assignment() {
        return new DriverAssignmentService(drivers, orders, event -> { });
    }

    private Order placedOrder() {
        return orderService.placeOrder(layan, "manual-04", Map.of("manual-04-1", 2),
                OrderTestData.campusAddress(), false, null);
    }

    // ---------- customer ----------

    @Test
    void customerPlacesAnOrderAndSeesTheBill() {
        customerScreen("1", "1", "2", "2", "1", "", "1", "no", "NAJAH10").placeOrder(layan);

        assertTrue(printed.contains(" 1) Jaltah"), printed);
        assertTrue(printed.contains("Order O1 placed: Pending"), printed);
        assertTrue(printed.contains("Food:         68.00 ILS"));
        assertTrue(printed.contains("Discount:    -6.80 ILS"));
        assertTrue(printed.contains("Delivery fee: 5.00 ILS (Promotion 10%)"));
        assertTrue(printed.contains("Total:        66.20 ILS, cash on delivery"));
    }

    @Test
    void customerCanChooseAnotherPlaceAndExpressDelivery() {
        customerScreen("1", "1", "1", "", "3", "Dorm", "32.2190", "35.2401", "yes", "").placeOrder(layan);

        assertTrue(printed.contains("Delivery fee: 15.0"), printed);
    }

    @Test
    void customerMistakesShowErrors() {
        customerScreen("9").placeOrder(layan);
        assertTrue(printed.contains("Error: please choose a number from 1 to 1"));

        customerScreen("1", "", "1", "no", "").placeOrder(layan);
        assertTrue(printed.contains("Error: An order must contain at least one item"));

        customerScreen("1", "7", "1", "1", "", "1", "no", "").placeOrder(layan);
        assertTrue(printed.contains("Error: please choose a number from 1 to 2"), printed);

        customerScreen("1", "1", "1", "", "9").placeOrder(layan);
        assertTrue(printed.contains("Error: Invalid delivery address choice"));

        customerScreen("1", "1", "1", "", "3", "Dorm", "north").placeOrder(layan);
        assertTrue(printed.contains("Error: 'north' is not a number"));

        customerScreen("1", "2", "1", "", "1", "no", "").placeOrder(layan);
        assertTrue(printed.contains("Error: The minimum order is 20 ILS"), printed);
    }

    @Test
    void noRestaurantTakingOrders() {
        restaurants = new InMemoryRestaurantRepository();

        customerScreen().placeOrder(layan);

        assertTrue(printed.contains("No restaurant can take orders right now."));
    }

    @Test
    void customerSeesAndCancelsOrders() {
        customerScreen().myOrders(layan);
        assertTrue(printed.contains("You have no orders yet."));

        Order order = placedOrder();
        customerScreen("").myOrders(layan);
        assertTrue(printed.contains(" O1 | Jaltah | Pending | 61.00 ILS | unpaid"), printed);

        customerScreen("O1").myOrders(layan);
        assertTrue(printed.contains("Order O1 is Cancelled"));
        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        customerScreen("O1").myOrders(layan);
        assertTrue(printed.contains("Error: Order O1 is Cancelled and can no longer be cancelled"), printed);
    }

    @Test
    void consolesNeedTheirServices() {
        RestaurantService restaurantService = new RestaurantService(restaurants, () -> OrderTestData.MONDAY_4PM);
        MenuService menus = new MenuService(restaurants);
        Scanner in = new Scanner("");

        assertThrows(IllegalArgumentException.class,
                () -> new OrderConsole(null, restaurantService, menus, in, System.out));
        assertThrows(IllegalArgumentException.class,
                () -> new RestaurantOrdersConsole(orderService, null, in, System.out));
    }

    // ---------- manager ----------

    @Test
    void managerRunsAnOrderFromStartToCashPayment() {
        drivers.save(new Driver("D4", "Yousef", OrderTestData.CAMPUS, 30));
        Order order = placedOrder();

        managerScreen("1", "2", "O1", "2", "O1", "2", "O1", "3", "O1", "2", "2", "O1", "4", "O1", "70", "2", "O1",
                "0");

        assertTrue(printed.contains(" O1 | Jaltah | Pending"), printed);
        assertTrue(printed.contains("Order O1 is now Confirmed"));
        assertTrue(printed.contains("Fastest driver: Yousef will deliver order O1"), printed);
        assertTrue(printed.contains("Order O1 is now Out for delivery"), printed);
        assertTrue(printed.contains("Paid. Change: 9.00 ILS"), printed);
        assertTrue(printed.contains("Order O1 is now Delivered"));
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
    }

    @Test
    void managerMistakesShowErrors() {
        Order order = placedOrder();

        managerScreen("1", "9", "2", "O9", "3", "O1", "7", "3", "O1", "1", "4", "O1", "lots", "4", "O1", "10", "0");

        assertTrue(printed.contains("Unknown choice: 9"));
        assertTrue(printed.contains("Error: Order not found: O9"));
        assertTrue(printed.contains("Error: Please choose 1 or 2"));
        assertTrue(printed.contains("Error: No available driver can deliver order O1 right now"), printed);
        assertTrue(printed.contains("Error: Please type an amount like 45.50"));
        assertTrue(printed.contains("Error: Cash is paid on delivery"), printed);
    }

    @Test
    void managerSeesNoOrdersAndFinishedOrders() {
        managerScreen("1", "0");
        assertTrue(printed.contains("No orders yet."));

        Order order = placedOrder();
        order.moveTo(OrderStatus.CANCELLED);
        managerScreen("2", "O1", "0");
        assertTrue(printed.contains("Error: Order O1 is finished"), printed);
    }

    @Test
    void rejectedCashPaymentIsShown() {
        drivers.save(new Driver("D4", "Yousef", OrderTestData.CAMPUS, 30));
        Order order = placedOrder();
        managerScreen("2", "O1", "2", "O1", "2", "O1", "3", "O1", "1", "2", "O1", "4", "O1", "50", "0");

        assertTrue(printed.contains("Nearest driver: Yousef will deliver order O1"), printed);
        assertTrue(printed.contains("Payment rejected: the amount is less than the total."), printed);
        assertEquals(PaymentStatus.REJECTED, order.getPaymentStatus());
    }

    @Test
    void managerCannotTouchAnotherRestaurantsOrder() {
        User otherManager = new User("ward", "x", Role.MANAGER, "manual-02", "w@example.com");
        placedOrder();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        new RestaurantOrdersConsole(orderService, assignment(), new Scanner("2\nO1\n0\n"),
                new PrintStream(output, true, StandardCharsets.UTF_8)).run(otherManager);

        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Error: Order not found: O1"));
    }
}
