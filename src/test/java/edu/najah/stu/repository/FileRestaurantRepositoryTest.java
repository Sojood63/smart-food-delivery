package edu.najah.stu.repository;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.HoursParser;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.WeeklySchedule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileRestaurantRepositoryTest {

    @TempDir
    Path folder;

    private Path restaurantsFile() {
        return folder.resolve("restaurants.txt");
    }

    private Path menuFile() {
        return folder.resolve("menu_items.txt");
    }

    private FileRestaurantRepository open() {
        return new FileRestaurantRepository(restaurantsFile(), menuFile());
    }

    private static Restaurant jaltah() {
        Restaurant jaltah = new Restaurant("manual-04", "Jaltah",
                HoursParser.parse("Mo-Th,Sa-Su 11:30-01:00; Fr 12:30-02:00", HoursSource.MANUAL),
                new Location(32.2234638, 35.2397614), Cuisine.BURGER);
        jaltah.addMenuItem(new MenuItem("manual-04-1", "Classic beef burger", new BigDecimal("25"), true));
        jaltah.addMenuItem(new MenuItem("manual-04-2", "Onion rings", new BigDecimal("12.5"), false));
        return jaltah;
    }

    private void writeRestaurants(String... lines) throws IOException {
        Files.write(restaurantsFile(), List.of(lines), StandardCharsets.UTF_8);
    }

    // S1
    @Test
    void savedRestaurantComesBackAfterReopening() {
        open().save(jaltah());

        Restaurant loaded = open().findById("manual-04").orElseThrow();

        assertEquals("Jaltah", loaded.getName());
        assertEquals(Cuisine.BURGER, loaded.getCuisine());
        assertEquals(HoursSource.MANUAL, loaded.getSchedule().getSource());
        assertEquals(LocalTime.of(2, 0), loaded.getSchedule().hoursOn(DayOfWeek.FRIDAY).orElseThrow().getClosesAt());
        assertEquals(32.2234638, loaded.getLocation().orElseThrow().getLatitude());
        assertEquals(35.2397614, loaded.getLocation().orElseThrow().getLongitude());
    }

    @Test
    void menuComesBackWithExactPricesAndAvailability() {
        open().save(jaltah());

        List<MenuItem> menu = open().findById("manual-04").orElseThrow().getMenuItems();

        assertEquals(2, menu.size());
        assertEquals("Classic beef burger", menu.get(0).getName());
        assertEquals(new BigDecimal("25.00"), menu.get(0).getPrice());
        assertEquals(new BigDecimal("12.50"), menu.get(1).getPrice());
        assertFalse(menu.get(1).isAvailable());
    }

    // S2
    @Test
    void missingFilesMeanAnEmptyRepository() {
        assertTrue(open().findAll().isEmpty());
        assertFalse(Files.exists(restaurantsFile()));
    }

    @Test
    void firstSaveCreatesTheFilesAndFolder() {
        Path nested = folder.resolve("new-folder");
        new FileRestaurantRepository(nested.resolve("r.txt"), nested.resolve("m.txt")).save(jaltah());

        assertTrue(Files.exists(nested.resolve("r.txt")));
        assertTrue(Files.exists(nested.resolve("m.txt")));
    }

    // S3
    @Test
    void restaurantWithoutLocationStaysWithoutLocation() {
        open().save(new Restaurant("manual-01", "W Restaurant",
                HoursParser.parse("Mo-Su 10:00-23:00", HoursSource.MANUAL), null, Cuisine.OTHER));

        assertTrue(open().findById("manual-01").orElseThrow().getLocation().isEmpty());
    }

    // S4
    @Test
    void allDayUnknownAndEstimatedHoursKeepTheirKind() {
        open().saveAll(List.of(
                new Restaurant("a", "All day", WeeklySchedule.allDay(HoursSource.OSM), null, Cuisine.GRILL),
                new Restaurant("u", "Unknown", WeeklySchedule.unknown(), null, Cuisine.OTHER),
                new Restaurant("e", "Estimated",
                        HoursParser.parse("Mo-Su 10:00-02:00", HoursSource.ESTIMATED), null, Cuisine.SHAWARMA)));

        FileRestaurantRepository reopened = open();

        assertTrue(reopened.findById("a").orElseThrow().getSchedule().isOpenAllDay());
        assertEquals(HoursSource.OSM, reopened.findById("a").orElseThrow().getSchedule().getSource());
        assertEquals(HoursSource.UNKNOWN, reopened.findById("u").orElseThrow().getSchedule().getSource());
        assertEquals(HoursSource.ESTIMATED, reopened.findById("e").orElseThrow().getSchedule().getSource());
    }

    // S5
    @Test
    void savingTheSameIdAgainReplacesIt() throws IOException {
        FileRestaurantRepository repository = open();
        repository.save(jaltah());
        repository.save(new Restaurant("manual-04", "Jaltah (renamed)",
                WeeklySchedule.unknown(), null, Cuisine.BURGER));

        assertEquals(1, open().findAll().size());
        assertEquals("Jaltah (renamed)", open().findById("manual-04").orElseThrow().getName());
        try (Stream<String> lines = Files.lines(restaurantsFile())) {
            assertEquals(1, lines.filter(line -> line.startsWith("manual-04|")).count());
        }
    }

    @Test
    void keepsSavingOrder() {
        open().saveAll(List.of(
                new Restaurant("b", "Second", WeeklySchedule.unknown(), null, Cuisine.OTHER),
                new Restaurant("a", "First", WeeklySchedule.unknown(), null, Cuisine.OTHER)));

        assertEquals(List.of("b", "a"), open().findAll().stream().map(Restaurant::getId).toList());
    }

    // S6
    @Test
    void badLineNamesFileAndLineNumber() throws IOException {
        writeRestaurants("# header", "manual-01|W Restaurant|NOT_A_CUISINE|Mo-Su 10:00-23:00|MANUAL||");

        DataFormatException error = assertThrows(DataFormatException.class, this::open);
        assertTrue(error.getMessage().startsWith("restaurants.txt line 2:"), error.getMessage());
    }

    @Test
    void unreadableHoursAreAnError() throws IOException {
        writeRestaurants("manual-01|W Restaurant|OTHER|whenever|MANUAL||");

        assertThrows(DataFormatException.class, this::open);
    }

    @Test
    void duplicateIdInFileIsAnError() throws IOException {
        writeRestaurants("a|One|OTHER||UNKNOWN||", "a|Two|OTHER||UNKNOWN||");

        assertThrows(DataFormatException.class, this::open);
    }

    @Test
    void badCoordinatesAreAnError() throws IOException {
        writeRestaurants("a|One|OTHER||UNKNOWN|north|35.2");

        assertThrows(DataFormatException.class, this::open);
    }

    // S7
    @Test
    void menuItemForUnknownRestaurantIsAnError() throws IOException {
        writeRestaurants("a|One|OTHER||UNKNOWN||");
        Files.write(menuFile(), List.of("a-1|zzz|Burger|20|yes"), StandardCharsets.UTF_8);

        DataFormatException error = assertThrows(DataFormatException.class, this::open);
        assertEquals("menu_items.txt line 1: menu item for unknown restaurant zzz", error.getMessage());
    }

    @Test
    void badMenuPriceIsAnError() throws IOException {
        writeRestaurants("a|One|OTHER||UNKNOWN||");
        Files.write(menuFile(), List.of("a-1|a|Burger|-5|yes"), StandardCharsets.UTF_8);

        assertThrows(DataFormatException.class, this::open);
    }

    // S8
    @Test
    void rejectsTextThatWouldBreakTheFile() {
        FileRestaurantRepository repository = open();
        Restaurant pipe = new Restaurant("a", "Pizza | Pasta", WeeklySchedule.unknown(), null, Cuisine.PIZZA);
        Restaurant newLine = new Restaurant("b", "Two\nlines", WeeklySchedule.unknown(), null, Cuisine.PIZZA);

        assertThrows(IllegalArgumentException.class, () -> repository.save(pipe));
        assertThrows(IllegalArgumentException.class, () -> repository.save(newLine));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void rejectsPipeInMenuItemName() {
        Restaurant restaurant = jaltah();
        restaurant.addMenuItem(new MenuItem("manual-04-3", "Fries | large", BigDecimal.TEN, true));
        FileRestaurantRepository repository = open();

        assertThrows(IllegalArgumentException.class, () -> repository.save(restaurant));
    }

    @Test
    void rejectsNullRestaurantAndPaths() {
        FileRestaurantRepository repository = open();

        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
        assertThrows(IllegalArgumentException.class, () -> new FileRestaurantRepository(null, menuFile()));
        assertThrows(IllegalArgumentException.class, () -> new FileRestaurantRepository(restaurantsFile(), null));
    }

    // S9
    @Test
    void keepsArabicNames() {
        open().save(new Restaurant("manual-63", "مطعم خميس", WeeklySchedule.unknown(), null, Cuisine.OTHER));

        assertEquals("مطعم خميس", open().findById("manual-63").orElseThrow().getName());
    }

    @Test
    void leavesNoTemporaryFilesBehind() throws IOException {
        open().save(jaltah());

        try (Stream<Path> files = Files.list(folder)) {
            assertEquals(List.of("menu_items.txt", "restaurants.txt"),
                    files.map(path -> path.getFileName().toString()).sorted().toList());
        }
    }

    @Test
    void findByUnknownIdIsEmpty() {
        assertTrue(open().findById("nope").isEmpty());
    }
}
