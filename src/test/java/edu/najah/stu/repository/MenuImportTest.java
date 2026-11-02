package edu.najah.stu.repository;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reading the team's menus ({@code menus.txt}) and attaching them to restaurants.
 */
class MenuImportTest {

    private final RestaurantImporter importer = RestaurantImporter.withDefaultStrategies();

    private static PipeFile.Row row(String line) {
        return PipeFile.read(new BufferedReader(new StringReader(line)), "menus.txt", MenuRow.COLUMNS).get(0);
    }

    private static ManualEntry entry(String id) {
        return new ManualEntry(id, "Restaurant " + id, Cuisine.BURGER, "Mo-Su 10:00-23:00", "", "");
    }

    @Test
    void readsAMenuLine() {
        MenuRow menuRow = MenuRow.fromRow(row("manual-04|Classic beef burger|25|yes"));

        assertEquals("manual-04", menuRow.restaurantId());
        assertEquals("Classic beef burger", menuRow.itemName());
        assertEquals(new BigDecimal("25"), menuRow.price());
        assertTrue(menuRow.available());
    }

    @Test
    void acceptsYesNoTrueFalseInAnyCase() {
        assertFalse(MenuRow.fromRow(row("a|Fries|10|NO")).available());
        assertTrue(MenuRow.fromRow(row("a|Fries|10|True")).available());
        assertFalse(MenuRow.fromRow(row("a|Fries|10|false")).available());
    }

    @Test
    void rejectsBadMenuLines() {
        List<PipeFile.Row> badRows = List.of(
                row("|Fries|10|yes"),
                row("a||10|yes"),
                row("a|Fries|ten|yes"),
                row("a|Fries|0|yes"),
                row("a|Fries|-3|yes"),
                row("a|Fries|10|maybe"));

        for (PipeFile.Row badRow : badRows) {
            assertThrows(DataFormatException.class, () -> MenuRow.fromRow(badRow), badRow.fields().toString());
        }
    }

    @Test
    void attachesMenuItemsWithNumberedIds() {
        List<Restaurant> restaurants = importer.importRestaurants(List.of(entry("manual-04"), entry("manual-90")),
                List.of(), List.of(
                        new MenuRow("manual-04", "Classic beef burger", new BigDecimal("25"), true),
                        new MenuRow("manual-04", "Onion rings", new BigDecimal("12.5"), false)));

        List<MenuItem> menu = restaurants.get(0).getMenuItems();
        assertEquals(List.of("manual-04-1", "manual-04-2"), menu.stream().map(MenuItem::getId).toList());
        assertEquals(new BigDecimal("12.50"), menu.get(1).getPrice());
        assertTrue(restaurants.get(1).getMenuItems().isEmpty());
    }

    @Test
    void rejectsMenuForUnknownRestaurant() {
        List<ManualEntry> entries = List.of(entry("manual-04"));
        List<OsmPlace> places = List.of();
        List<MenuRow> menus = List.of(new MenuRow("manual-99", "Fries", BigDecimal.TEN, true));

        DataFormatException error = assertThrows(DataFormatException.class,
                () -> importer.importRestaurants(entries, places, menus));
        assertEquals("menus.txt: unknown restaurant id manual-99", error.getMessage());
    }

    @Test
    void bundledMenuFileIsReadable() {
        List<Restaurant> restaurants = importer.importBundledData();

        assertEquals(66, restaurants.size());
    }
}
