package edu.najah.stu.repository;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.User;
import edu.najah.stu.domain.WeeklySchedule;
import edu.najah.stu.event.EventBus;
import edu.najah.stu.event.SystemEvent;
import edu.najah.stu.service.RestaurantManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The manager's changes are written to the text files and are still there
 * after the program is closed and opened again.
 */
class ManagerChangesPersistTest {

    @TempDir
    Path folder;

    private final User owner = new User("sojood", "pbkdf2$1$c2FsdA==$aGFzaA==", Role.MANAGER, "manual-10",
            "sojood@example.com");
    private final List<SystemEvent> published = new ArrayList<>();
    private final EventBus events = new EventBus();

    @BeforeEach
    void listenToEvents() {
        events.subscribe(published::add);
    }

    private FileRestaurantRepository open() {
        return new FileRestaurantRepository(folder.resolve("restaurants.txt"), folder.resolve("menu_items.txt"));
    }

    private static Restaurant restaurant(String id) {
        Restaurant restaurant = new Restaurant(id, "Place " + id, WeeklySchedule.unknown(), null, Cuisine.GRILL);
        restaurant.addMenuItem(new MenuItem(id + "-1", "Kebab", new BigDecimal("20"), true));
        return restaurant;
    }

    @Test
    void everyKindOfChangeSurvivesARestart() {
        FileRestaurantRepository repository = open();
        repository.save(restaurant("manual-10"));
        RestaurantManagementService manager = new RestaurantManagementService(repository, events);

        manager.updateHours(owner, "Sa-Th 10:00-01:00; Fr 13:00-01:00");
        manager.updateLocation(owner, 32.2234638, 35.2397614);
        manager.addMenuItem(owner, "Grilled chicken", new BigDecimal("35"));
        manager.changePrice(owner, "manual-10-1", new BigDecimal("22.5"));
        manager.setAvailable(owner, "manual-10-2", false);

        Restaurant reopened = open().findById("manual-10").orElseThrow();
        assertEquals(HoursSource.MANUAL, reopened.getSchedule().getSource());
        assertTrue(reopened.getSchedule().hoursOn(DayOfWeek.FRIDAY).orElseThrow().closesOvernight());
        assertEquals(32.2234638, reopened.getLocation().orElseThrow().getLatitude());
        assertEquals(new BigDecimal("22.50"), reopened.findMenuItem("manual-10-1").orElseThrow().getPrice());
        assertFalse(reopened.findMenuItem("manual-10-2").orElseThrow().isAvailable());
        assertEquals(List.of("Opening hours updated: Place manual-10", "Location updated: Place manual-10",
                        "New menu item: Grilled chicken", "Price changed: Kebab", "Grilled chicken is now unavailable"),
                published.stream().map(SystemEvent::subject).toList());
    }

    @Test
    void removedItemStaysRemovedAndItsNumberIsNotReused() {
        FileRestaurantRepository repository = open();
        repository.save(restaurant("manual-10"));
        RestaurantManagementService manager = new RestaurantManagementService(repository, events);
        manager.addMenuItem(owner, "Salad", new BigDecimal("7"));

        manager.removeMenuItem(owner, "manual-10-1");
        RestaurantManagementService afterRestart = new RestaurantManagementService(open(), events);
        MenuItem added = afterRestart.addMenuItem(owner, "Soup", new BigDecimal("9"));

        assertEquals("manual-10-3", added.getId());
        assertEquals(List.of("manual-10-2", "manual-10-3"),
                open().findById("manual-10").orElseThrow().getMenuItems().stream().map(MenuItem::getId).toList());
    }

    @Test
    void replaceAllForgetsOldRestaurantsAndChanges() {
        FileRestaurantRepository repository = open();
        repository.saveAll(List.of(restaurant("manual-10"), restaurant("manual-11")));
        new RestaurantManagementService(repository, events).changePrice(owner, "manual-10-1", BigDecimal.ONE);

        repository.replaceAll(List.of(restaurant("manual-10")));

        List<Restaurant> reopened = open().findAll();
        assertEquals(1, reopened.size());
        assertEquals(new BigDecimal("20.00"), reopened.get(0).getMenuItems().get(0).getPrice());
    }

    @Test
    void replaceAllRejectsBadDataAndKeepsTheOldData() {
        FileRestaurantRepository repository = open();
        repository.save(restaurant("manual-10"));
        Restaurant broken = new Restaurant("manual-12", "Bad | name", WeeklySchedule.unknown(), null, Cuisine.OTHER);
        List<Restaurant> withBroken = List.of(restaurant("manual-11"), broken);

        assertThrows(IllegalArgumentException.class, () -> repository.replaceAll(withBroken));

        assertEquals(List.of("manual-10"), repository.findAll().stream().map(Restaurant::getId).toList());
        assertEquals(1, open().findAll().size());
    }
}
