package edu.najah.stu.presentation;

import edu.najah.stu.OrderTestData;
import edu.najah.stu.domain.Order;
import edu.najah.stu.domain.OrderStatus;
import edu.najah.stu.domain.User;
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
 * Drives the customer's ordering screens with typed answers.
 */
class OrderConsolesTest {

    private final User layan = OrderTestData.customer();
    private final User sojood = OrderTestData.jaltahManager();
    private InMemoryRestaurantRepository restaurants;
    private InMemoryOrderRepository orders;
    private OrderService orderService;
    private String printed;

    @BeforeEach
    void setUp() {
        restaurants = OrderTestData.restaurants();
        orders = new InMemoryOrderRepository();
        orderService = new OrderService(restaurants, orders, OrderTestData.users(layan, sojood),
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

    private Order placedOrder() {
        return orderService.placeOrder(layan, "manual-04", Map.of("manual-04-1", 2),
                OrderTestData.campusAddress());
    }

    // ---------- customer ----------

    @Test
    void customerPlacesAnOrderAndSeesTheTotal() {
        customerScreen("1", "1", "2", "2", "1", "", "1").placeOrder(layan);

        assertTrue(printed.contains(" 1) Jaltah"), printed);
        assertTrue(printed.contains("Order O1 placed: Pending"), printed);
        assertTrue(printed.contains("Total: 68.00 ILS"), printed);
    }

    @Test
    void customerCanChooseAnotherPlace() {
        customerScreen("1", "1", "1", "", "3", "Dorm", "32.2190", "35.2401").placeOrder(layan);

        assertTrue(printed.contains("Order O1 placed"), printed);
    }

    @Test
    void customerMistakesShowErrors() {
        customerScreen("9").placeOrder(layan);
        assertTrue(printed.contains("Error: please choose a number from 1 to 1"));

        customerScreen("1", "", "1").placeOrder(layan);
        assertTrue(printed.contains("Error: An order must contain at least one item"));

        customerScreen("1", "7", "1", "1", "", "1").placeOrder(layan);
        assertTrue(printed.contains("Error: please choose a number from 1 to 2"), printed);

        customerScreen("1", "1", "1", "", "9").placeOrder(layan);
        assertTrue(printed.contains("Error: Invalid delivery address choice"));

        customerScreen("1", "1", "1", "", "3", "Dorm", "north").placeOrder(layan);
        assertTrue(printed.contains("Error: 'north' is not a number"));

        customerScreen("1", "2", "1", "", "1").placeOrder(layan);
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
        assertTrue(printed.contains(" O1 | Jaltah | Pending | 56.00 ILS"), printed);

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
    }
}
