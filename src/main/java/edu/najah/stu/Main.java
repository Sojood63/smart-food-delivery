package edu.najah.stu;

import edu.najah.stu.delivery.DriverAssignmentService;
import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.User;
import edu.najah.stu.event.EventBus;
import edu.najah.stu.notification.EmailEventListener;
import edu.najah.stu.notification.MailSenders;
import edu.najah.stu.payment.CashPaymentService;
import edu.najah.stu.presentation.CustomerConsole;
import edu.najah.stu.presentation.LoginConsole;
import edu.najah.stu.presentation.ManagerConsole;
import edu.najah.stu.presentation.OrderConsole;
import edu.najah.stu.presentation.RestaurantOrdersConsole;
import edu.najah.stu.pricing.PricingService;
import edu.najah.stu.repository.BundledUsers;
import edu.najah.stu.repository.DataStore;
import edu.najah.stu.repository.FileRestaurantRepository;
import edu.najah.stu.repository.FileUserRepository;
import edu.najah.stu.repository.InMemoryDriverRepository;
import edu.najah.stu.repository.InMemoryOrderRepository;
import edu.najah.stu.repository.RestaurantImporter;
import edu.najah.stu.service.AuthService;
import edu.najah.stu.service.MenuService;
import edu.najah.stu.service.OrderService;
import edu.najah.stu.service.Pbkdf2PasswordHasher;
import edu.najah.stu.service.RestaurantManagementService;
import edu.najah.stu.service.RestaurantService;
import edu.najah.stu.service.SystemTimeProvider;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Scanner;

/**
 * Starts the program: loads the data, then shows the login screen and the
 * customer or manager menu.
 *
 * <p>This class only connects the layers; all the logic is in the services,
 * which have their own tests.</p>
 */
public class Main {

    private static final Path DATA_FOLDER = Path.of("data");
    private static final Path MAIL_SETTINGS = Path.of("mail.properties");
    private static final String MANAGER_MENU = String.join(System.lineSeparator(),
            "",
            "1) Manage my restaurant",
            "2) Orders of my restaurant",
            "3) Open restaurants near An-Najah",
            "4) Reload the bundled data (deletes all manager changes)",
            "0) Log out");

    private final RestaurantImporter importer = RestaurantImporter.withDefaultStrategies();
    private final FileRestaurantRepository restaurants = DataStore.open(DATA_FOLDER, importer::importBundledData);
    private final SystemTimeProvider clock = new SystemTimeProvider();
    private final RestaurantService restaurantService = new RestaurantService(restaurants, clock);
    private final MenuService menuService = new MenuService(restaurants);
    private final Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
    private final EventBus events = new EventBus();
    private final FileUserRepository users = DataStore.openUsers(DATA_FOLDER, BundledUsers::load);
    private final InMemoryOrderRepository orders = new InMemoryOrderRepository();
    private final OrderService orderService = new OrderService(restaurants, orders, users,
            new PricingService(clock), new CashPaymentService(), clock, events);
    private final CustomerConsole customerConsole = new CustomerConsole(restaurantService, menuService,
            new OrderConsole(orderService, restaurantService, menuService, in, System.out), in, System.out);
    private final ManagerConsole managerConsole =
            new ManagerConsole(new RestaurantManagementService(restaurants, events), in, System.out);
    private final RestaurantOrdersConsole restaurantOrdersConsole = new RestaurantOrdersConsole(orderService,
            new DriverAssignmentService(InMemoryDriverRepository.withDemoDrivers(), orders, events), in, System.out);

    /**
     * Runs the program in the console.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        new Main().start();
    }

    private void start() {
        events.subscribe(new EmailEventListener(MailSenders.fromSettings(System.getenv(), MAIL_SETTINGS, System.out)));
        AuthService authService = new AuthService(users, new Pbkdf2PasswordHasher(), clock, events);
        LoginConsole loginConsole = new LoginConsole(authService, in, System.out);

        System.out.println("Loaded " + restaurants.findAll().size() + " restaurants and " + users.findAll().size()
                + " accounts from " + DATA_FOLDER.toAbsolutePath());
        Optional<User> user = loginConsole.run();
        while (user.isPresent()) {
            if (user.get().getRole() == Role.MANAGER) {
                managerMenu(user.get());
            } else {
                customerConsole.run(user.get());
            }
            System.out.println("Logged out.");
            user = loginConsole.run();
        }
        System.out.println("Map data (c) OpenStreetMap contributors");
    }

    private String ask(String menu) {
        System.out.println(menu);
        return in.hasNextLine() ? in.nextLine().trim() : "0";
    }

    private void managerMenu(User manager) {
        String choice = ask(MANAGER_MENU);
        while (!choice.equals("0")) {
            switch (choice) {
                case "1" -> managerConsole.run(manager);
                case "2" -> restaurantOrdersConsole.run(manager);
                case "3" -> customerConsole.printNearby();
                case "4" -> reload();
                default -> System.out.println("Unknown choice: " + choice);
            }
            choice = ask(MANAGER_MENU);
        }
    }

    private void reload() {
        System.out.println("This deletes every change made in the manager panel. Type yes to continue:");
        if (in.hasNextLine() && in.nextLine().trim().equalsIgnoreCase("yes")) {
            restaurants.replaceAll(importer.importBundledData());
            System.out.println("Reloaded " + restaurants.findAll().size() + " restaurants.");
        } else {
            System.out.println("Nothing changed.");
        }
    }
}
