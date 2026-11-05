package edu.najah.stu.presentation;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.Landmark;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.OpeningHours;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.WeeklySchedule;
import edu.najah.stu.repository.InMemoryRestaurantRepository;
import edu.najah.stu.service.MenuService;
import edu.najah.stu.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the customer menu with typed answers (US1.2 and US1.3).
 * The clock is fixed at Monday noon.
 */
class CustomerConsoleTest {

    private static final LocalDateTime MONDAY_NOON = LocalDateTime.of(2026, 11, 2, 12, 0);
    private static final WeeklySchedule DAYTIME =
            WeeklySchedule.everyDay(new OpeningHours(LocalTime.of(9, 0), LocalTime.of(22, 0)), HoursSource.MANUAL);
    private static final WeeklySchedule NIGHT_ONLY =
            WeeklySchedule.everyDay(new OpeningHours(LocalTime.of(18, 0), LocalTime.of(2, 0)), HoursSource.MANUAL);

    private InMemoryRestaurantRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRestaurantRepository();
        Restaurant jaltah = new Restaurant("manual-04", "Jaltah", DAYTIME,
                new Location(32.2234638, 35.2397614), Cuisine.BURGER);
        jaltah.addMenuItem(new MenuItem("manual-04-1", "كلاسيك برغر", new BigDecimal("28"), true));
        jaltah.addMenuItem(new MenuItem("manual-04-2", "Onion rings", new BigDecimal("12.5"), false));
        jaltah.addMenuItem(new MenuItem("manual-04-3", "Fries", new BigDecimal("12"), true));
        Restaurant nightPlace = new Restaurant("manual-05", "Night Grill", NIGHT_ONLY, null, Cuisine.GRILL);
        nightPlace.addMenuItem(new MenuItem("manual-05-1", "Kebab", new BigDecimal("40"), true));
        Restaurant noMenu = new Restaurant("manual-06", "Nafas", DAYTIME, null, Cuisine.CAFE);
        Restaurant soldOut = new Restaurant("manual-07", "Sold Out Sweets", DAYTIME, null, Cuisine.SWEETS);
        soldOut.addMenuItem(new MenuItem("manual-07-1", "Kunafa", new BigDecimal("10"), false));
        repository.save(jaltah);
        repository.save(nightPlace);
        repository.save(noMenu);
        repository.save(soldOut);
    }

    /** Runs the customer menu with the given lines typed in, and returns everything printed. */
    private String type(String... lines) {
        String input = String.join("\n", lines) + "\n";
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        new CustomerConsole(new RestaurantService(repository, () -> MONDAY_NOON), new MenuService(repository),
                new Scanner(input), new PrintStream(printed, true, StandardCharsets.UTF_8)).run();
        return printed.toString(StandardCharsets.UTF_8);
    }

    @Test
    void rejectsMissingParts() {
        RestaurantService restaurants = new RestaurantService(repository, () -> MONDAY_NOON);
        MenuService menus = new MenuService(repository);
        Scanner in = new Scanner("");
        PrintStream out = System.out;

        assertThrows(IllegalArgumentException.class, () -> new CustomerConsole(null, menus, in, out));
        assertThrows(IllegalArgumentException.class, () -> new CustomerConsole(restaurants, null, in, out));
        assertThrows(IllegalArgumentException.class, () -> new CustomerConsole(restaurants, menus, null, out));
    }

    // ---------- US1.3: view a restaurant's menu ----------

    @Test
    void listsOnlyOpenRestaurantsWithSomethingToOrder() {
        String output = type("2", "", "0");

        assertTrue(output.contains(" 1) Jaltah | Burger"));
        assertFalse(output.contains("Night Grill"), "closed restaurant must be hidden");
        assertFalse(output.contains("Nafas"), "restaurant without a menu is not offered");
        assertFalse(output.contains("Sold Out Sweets"), "restaurant with nothing available is not offered");
    }

    @Test
    void showsAvailableItemsWithPricesAndHidesUnavailableOnes() {
        String output = type("2", "1", "0");

        assertTrue(output.contains("Menu of Jaltah:"));
        assertTrue(output.contains("  كلاسيك برغر | 28.00 ILS"));
        assertTrue(output.contains("  Fries | 12.00 ILS"));
        assertFalse(output.contains("Onion rings"), "unavailable item must be hidden");
    }

    @Test
    void numberOutOfRangeOrNotANumberIsAnError() {
        String output = type("2", "5", "2", "two", "0");

        assertTrue(output.contains("Error: please choose a number from 1 to 1"));
        assertFalse(output.contains("Menu of"));
    }

    @Test
    void noOpenRestaurantWithAMenu() {
        repository = new InMemoryRestaurantRepository();

        assertTrue(type("2", "0").contains("No open restaurant has a menu right now."));
    }

    // ---------- US1.2: open restaurants near the university ----------

    @Test
    void nearbyListShowsOpenRestaurantsAndThoseWithoutLocation() {
        String output = type("1", "0");

        assertTrue(output.contains("Open restaurants within 1.5 km of " + Landmark.OLD_CAMPUS.getDisplayName()));
        assertTrue(output.contains(" * Jaltah | Burger |"));
        assertTrue(output.contains("Also open now, location not known yet (2):"));
        assertTrue(output.contains(" * Nafas"));
        assertFalse(output.contains("Night Grill"));
    }

    @Test
    void nearbyListSaysWhenNothingIsOpenAndShortensLongLists() {
        repository = new InMemoryRestaurantRepository();
        for (int i = 1; i <= 12; i++) {
            repository.save(new Restaurant("r" + i, "Place " + i, DAYTIME, null, Cuisine.OTHER));
        }

        String output = type("1", "0");

        assertTrue(output.contains("   (none open right now)"));
        assertTrue(output.contains("   ... and 2 more"));
        assertFalse(output.contains("Place 11"));
    }

    @Test
    void unknownChoiceAndEndOfInput() {
        assertTrue(type("9", "0").contains("Unknown choice: 9"));
        assertFalse(type().contains("Menu of"));
    }
}
