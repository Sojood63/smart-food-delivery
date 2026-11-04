package edu.najah.stu.presentation;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.HoursParser;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.WeeklySchedule;
import edu.najah.stu.repository.InMemoryRestaurantRepository;
import edu.najah.stu.service.RestaurantManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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

    private RestaurantManagementService service;
    private Restaurant khamees;

    @BeforeEach
    void setUp() {
        InMemoryRestaurantRepository repository = new InMemoryRestaurantRepository();
        khamees = new Restaurant(ID, "Khamees", HoursParser.parse("Mo-Su 07:00-01:00", HoursSource.OSM),
                null, Cuisine.SHAWARMA);
        khamees.addMenuItem(new MenuItem(ID + "-1", "Chicken shawarma", new BigDecimal("12"), true));
        repository.save(khamees);
        service = new RestaurantManagementService(repository);
    }

    /** Runs the console with the given lines typed in, and returns everything it printed. */
    private String type(String... lines) {
        String input = String.join("\n", lines) + "\n";
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        new ManagerConsole(service, new Scanner(input), new PrintStream(printed, true, StandardCharsets.UTF_8)).run();
        return printed.toString(StandardCharsets.UTF_8);
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
    void emptyIdGoesBack() {
        String output = type("");

        assertFalse(output.contains("Managing"));
    }

    @Test
    void noInputAtAllGoesBack() {
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        new ManagerConsole(service, new Scanner(""), new PrintStream(printed, true, StandardCharsets.UTF_8)).run();

        assertFalse(printed.toString(StandardCharsets.UTF_8).contains("Managing"));
    }

    @Test
    void unknownRestaurantShowsAnError() {
        assertTrue(type("manual-99").contains("Error: Restaurant not found: manual-99"));
    }

    @Test
    void showsDetailsWithHoursSourceAndMenu() {
        String output = type(ID, "1", "0");

        assertTrue(output.contains("Managing Khamees (manual-63)"));
        assertTrue(output.contains("Khamees | Shawarma"));
        assertTrue(output.contains("Hours: Mo-Su 07:00-01:00 (osm)"));
        assertTrue(output.contains("Location: not known yet"));
        assertTrue(output.contains("manual-63-1 | Chicken shawarma | 12.00 ILS"));
    }

    @Test
    void showsUnknownHoursEmptyMenuAndUnavailableItems() {
        service.updateLocation(ID, 32.2243506, 35.2434154);
        service.setAvailable(ID, ID + "-1", false);
        String withUnavailable = type(ID, "1", "0");
        service.removeMenuItem(ID, ID + "-1");
        khamees.changeSchedule(WeeklySchedule.unknown());

        String output = type(ID, "1", "0");

        assertTrue(withUnavailable.contains("12.00 ILS | not available"));
        assertTrue(withUnavailable.contains("Location: 32.2243506, 35.2434154"));
        assertTrue(output.contains("Hours: not known"));
        assertTrue(output.contains("Menu: empty"));
    }

    @Test
    void changesHours() {
        String output = type(ID, "2", "Sa-Th 09:00-23:00; Fr off", "0");

        assertTrue(output.contains("Saved. Hours: Mo-Th,Sa-Su 09:00-23:00"), output);
        assertEquals(HoursSource.MANUAL, khamees.getSchedule().getSource());
    }

    @Test
    void badHoursShowTheExampleAndTheMenuContinues() {
        String output = type(ID, "2", "always open", "1", "0");

        assertTrue(output.contains("Error: Cannot read opening hours 'always open'"));
        assertTrue(output.contains("Hours: Mo-Su 07:00-01:00 (osm)"));
    }

    @Test
    void changesLocation() {
        String output = type(ID, "3", "32.2243506", "35.2434154", "0");

        assertTrue(output.contains("Saved. Location: 32.2243506, 35.2434154"));
        assertTrue(khamees.getLocation().isPresent());
    }

    @Test
    void locationThatIsNotANumberIsAnError() {
        String output = type(ID, "3", "north", "0");

        assertTrue(output.contains("Error: 'north' is not a number"));
        assertTrue(khamees.getLocation().isEmpty());
    }

    @Test
    void addsAnItem() {
        String output = type(ID, "4", "Kunafa", "10", "0");

        assertTrue(output.contains("Added Kunafa as manual-63-2"));
        assertEquals(2, khamees.getMenuItems().size());
    }

    @Test
    void priceThatIsNotANumberIsAnError() {
        String output = type(ID, "4", "Kunafa", "ten", "0");

        assertTrue(output.contains("Error: 'ten' is not a number"));
        assertEquals(1, khamees.getMenuItems().size());
    }

    @Test
    void changesAPrice() {
        String output = type(ID, "5", ID + "-1", "13.5", "0");

        assertTrue(output.contains("Saved. Chicken shawarma now costs 13.50 ILS"));
    }

    @Test
    void missingItemIsAnError() {
        String output = type(ID, "5", ID + "-9", "13", "0");

        assertTrue(output.contains("Error: Menu item manual-63-9 is not on the menu of manual-63"));
    }

    @Test
    void changesAvailabilityBothWays() {
        String output = type(ID, "6", ID + "-1", "no", "6", ID + "-1", "YES", "0");

        assertTrue(output.contains("Saved. Chicken shawarma is not available"));
        assertTrue(output.contains("Saved. Chicken shawarma is available"));
        assertTrue(khamees.getMenuItems().get(0).isAvailable());
    }

    @Test
    void availabilityNeedsYesOrNo() {
        String output = type(ID, "6", ID + "-1", "maybe", "0");

        assertTrue(output.contains("Error: Please answer yes or no"));
    }

    @Test
    void removesAnItem() {
        String output = type(ID, "7", ID + "-1", "0");

        assertTrue(output.contains("Removed manual-63-1"));
        assertTrue(khamees.getMenuItems().isEmpty());
    }

    @Test
    void unknownChoiceIsReported() {
        assertTrue(type(ID, "9", "0").contains("Unknown choice: 9"));
    }

    @Test
    void inputEndingInTheMiddleOfAChangeIsSafe() {
        String output = type(ID, "4");

        assertTrue(output.contains("Error: '' is not a number"));
        assertEquals(1, khamees.getMenuItems().size());
    }
}
