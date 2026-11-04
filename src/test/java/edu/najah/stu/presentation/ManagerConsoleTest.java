package edu.najah.stu.presentation;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.HoursParser;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.User;
import edu.najah.stu.domain.WeeklySchedule;
import edu.najah.stu.event.EventBus;
import edu.najah.stu.event.SystemEvent;
import edu.najah.stu.repository.InMemoryRestaurantRepository;
import edu.najah.stu.service.RestaurantManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the manager menu with typed answers, as if a manager were at the keyboard.
 */
class ManagerConsoleTest {

    private static final String ID = "manual-63";
    private static final String HASH = "pbkdf2$1$c2FsdA==$aGFzaA==";

    private final User sojood = managerOf(ID);
    private final List<SystemEvent> published = new ArrayList<>();
    private RestaurantManagementService service;
    private Restaurant khamees;

    @BeforeEach
    void setUp() {
        InMemoryRestaurantRepository repository = new InMemoryRestaurantRepository();
        khamees = new Restaurant(ID, "Khamees", HoursParser.parse("Mo-Su 07:00-01:00", HoursSource.OSM),
                null, Cuisine.SHAWARMA);
        khamees.addMenuItem(new MenuItem(ID + "-1", "Chicken shawarma", new BigDecimal("12"), true));
        repository.save(khamees);
        EventBus events = new EventBus();
        events.subscribe(published::add);
        service = new RestaurantManagementService(repository, events);
    }

    private static User managerOf(String restaurantId) {
        return new User("sojood", HASH, Role.MANAGER, restaurantId, "sojood@example.com");
    }

    /** Opens the manager panel for a user with the given lines typed in, and returns everything printed. */
    private String typeFor(User user, String... lines) {
        String input = String.join("\n", lines) + "\n";
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        new ManagerConsole(service, new Scanner(input), new PrintStream(printed, true, StandardCharsets.UTF_8))
                .run(user);
        return printed.toString(StandardCharsets.UTF_8);
    }

    /** Opens Khamees, the manager's own restaurant, with the given lines typed in. */
    private String type(String... lines) {
        return typeFor(sojood, lines);
    }

    @Test
    void rejectsMissingParts() {
        Scanner in = new Scanner("");
        PrintStream out = System.out;

        assertThrows(IllegalArgumentException.class, () -> new ManagerConsole(null, in, out));
        assertThrows(IllegalArgumentException.class, () -> new ManagerConsole(service, null, out));
        assertThrows(IllegalArgumentException.class, () -> new ManagerConsole(service, in, null));
    }

    @Test
    void customerCannotOpenTheManagerPanel() {
        User customer = new User("layan", HASH, Role.CUSTOMER, null, "layan@example.com");

        String output = typeFor(customer, "1");

        assertTrue(output.contains("Error: Only a restaurant manager can do this"));
        assertFalse(output.contains("Managing"));
    }

    @Test
    void noInputAtAllGoesBack() {
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        new ManagerConsole(service, new Scanner(""), new PrintStream(printed, true, StandardCharsets.UTF_8)).run(sojood);

        String output = printed.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Managing Khamees"));
        assertFalse(output.contains("Hours:"));
    }

    @Test
    void unknownRestaurantShowsAnError() {
        assertTrue(typeFor(managerOf("manual-99")).contains("Error: Restaurant not found: manual-99"));
    }

    @Test
    void showsDetailsWithHoursSourceAndMenu() {
        String output = type("1", "0");

        assertTrue(output.contains("Managing Khamees (manual-63)"));
        assertTrue(output.contains("Khamees | Shawarma"));
        assertTrue(output.contains("Hours: Mo-Su 07:00-01:00 (osm)"));
        assertTrue(output.contains("Location: not known yet"));
        assertTrue(output.contains("manual-63-1 | Chicken shawarma | 12.00 ILS"));
    }

    @Test
    void showsUnknownHoursEmptyMenuAndUnavailableItems() {
        service.updateLocation(sojood, 32.2243506, 35.2434154);
        service.setAvailable(sojood, ID + "-1", false);
        String withUnavailable = type("1", "0");
        service.removeMenuItem(sojood, ID + "-1");
        khamees.changeSchedule(WeeklySchedule.unknown());

        String output = type("1", "0");

        assertTrue(withUnavailable.contains("12.00 ILS | not available"));
        assertTrue(withUnavailable.contains("Location: 32.2243506, 35.2434154"));
        assertTrue(output.contains("Hours: not known"));
        assertTrue(output.contains("Menu: empty"));
    }

    @Test
    void changesHours() {
        String output = type("2", "Sa-Th 09:00-23:00; Fr off", "0");

        assertTrue(output.contains("Saved. Hours: Mo-Th,Sa-Su 09:00-23:00"), output);
        assertEquals(HoursSource.MANUAL, khamees.getSchedule().getSource());
        assertEquals("Opening hours updated: Khamees", published.get(0).subject());
    }

    @Test
    void badHoursShowTheExampleAndTheMenuContinues() {
        String output = type("2", "always open", "1", "0");

        assertTrue(output.contains("Error: Cannot read opening hours 'always open'"));
        assertTrue(published.isEmpty());
        assertTrue(output.contains("Hours: Mo-Su 07:00-01:00 (osm)"));
    }

    @Test
    void changesLocation() {
        String output = type("3", "32.2243506", "35.2434154", "0");

        assertTrue(output.contains("Saved. Location: 32.2243506, 35.2434154"));
        assertTrue(khamees.getLocation().isPresent());
    }

    @Test
    void locationThatIsNotANumberIsAnError() {
        String output = type("3", "north", "0");

        assertTrue(output.contains("Error: 'north' is not a number"));
        assertTrue(khamees.getLocation().isEmpty());
    }

    @Test
    void addsAnItem() {
        String output = type("4", "Kunafa", "10", "0");

        assertTrue(output.contains("Added Kunafa as manual-63-2"));
        assertEquals(2, khamees.getMenuItems().size());
    }

    @Test
    void priceThatIsNotANumberIsAnError() {
        String output = type("4", "Kunafa", "ten", "0");

        assertTrue(output.contains("Error: 'ten' is not a number"));
        assertEquals(1, khamees.getMenuItems().size());
    }

    @Test
    void changesAPrice() {
        String output = type("5", ID + "-1", "13.5", "0");

        assertTrue(output.contains("Saved. Chicken shawarma now costs 13.50 ILS"));
    }

    @Test
    void missingItemIsAnError() {
        String output = type("5", ID + "-9", "13", "0");

        assertTrue(output.contains("Error: Menu item manual-63-9 is not on the menu of manual-63"));
    }

    @Test
    void changesAvailabilityBothWays() {
        String output = type("6", ID + "-1", "no", "6", ID + "-1", "YES", "0");

        assertTrue(output.contains("Saved. Chicken shawarma is not available"));
        assertTrue(output.contains("Saved. Chicken shawarma is available"));
        assertTrue(khamees.getMenuItems().get(0).isAvailable());
    }

    @Test
    void availabilityNeedsYesOrNo() {
        String output = type("6", ID + "-1", "maybe", "0");

        assertTrue(output.contains("Error: Please answer yes or no"));
    }

    @Test
    void removesAnItem() {
        String output = type("7", ID + "-1", "0");

        assertTrue(output.contains("Removed manual-63-1"));
        assertTrue(khamees.getMenuItems().isEmpty());
    }

    @Test
    void unknownChoiceIsReported() {
        assertTrue(type("9", "0").contains("Unknown choice: 9"));
    }

    @Test
    void inputEndingInTheMiddleOfAChangeIsSafe() {
        String output = type("4");

        assertTrue(output.contains("Error: '' is not a number"));
        assertEquals(1, khamees.getMenuItems().size());
    }
}
