package edu.najah.stu.service;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.WeeklySchedule;
import edu.najah.stu.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The manager's changes: each good change is saved once, each bad one is
 * rejected and nothing is saved. The repository is a mock.
 */
@ExtendWith(MockitoExtension.class)
class RestaurantManagementServiceTest {

    private static final String ID = "manual-63";

    @Mock
    private RestaurantRepository repository;

    private RestaurantManagementService service;
    private Restaurant khamees;

    @BeforeEach
    void setUp() {
        service = new RestaurantManagementService(repository);
        khamees = new Restaurant(ID, "Khamees", WeeklySchedule.unknown(), null, Cuisine.SHAWARMA);
        khamees.addMenuItem(new MenuItem(ID + "-1", "Chicken shawarma", new BigDecimal("12"), true));
        khamees.addMenuItem(new MenuItem(ID + "-2", "Fries", new BigDecimal("8"), true));
    }

    private void restaurantExists() {
        when(repository.findById(any())).thenReturn(Optional.of(khamees));
    }

    /** Checks that a change was rejected and nothing was saved. */
    private <T extends Throwable> T rejected(Class<T> type, Executable change) {
        T error = assertThrows(type, change);
        verify(repository, never()).save(any());
        return error;
    }

    @Test
    void rejectsMissingRepository() {
        assertThrows(IllegalArgumentException.class, () -> new RestaurantManagementService(null));
    }

    // ---------- finding the restaurant ----------

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsEmptyRestaurantId(String id) {
        assertThrows(IllegalArgumentException.class, () -> service.getRestaurant(id));
    }

    @Test
    void unknownRestaurantIsReported() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        RestaurantNotFoundException error = rejected(RestaurantNotFoundException.class,
                () -> service.updateHours("manual-99", "24/7"));
        assertEquals("Restaurant not found: manual-99", error.getMessage());
    }

    // ---------- hours ----------

    @Test
    void savesNewHoursAsTheManagersOwn() {
        restaurantExists();

        WeeklySchedule schedule = service.updateHours(ID, "Sa-Th 09:00-23:00; Fr off");

        assertEquals(HoursSource.MANUAL, schedule.getSource());
        assertEquals(schedule, khamees.getSchedule());
        assertEquals(Optional.empty(), khamees.getSchedule().hoursOn(DayOfWeek.FRIDAY));
        assertEquals(LocalTime.of(23, 0),
                khamees.getSchedule().hoursOn(DayOfWeek.MONDAY).orElseThrow().getClosesAt());
        verify(repository).save(khamees);
    }

    @Test
    void acceptsOpenAllDay() {
        restaurantExists();

        assertTrue(service.updateHours(ID, "24/7").isOpenAllDay());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"always open", "Sa-Th 9 to 11", "Xx 09:00-23:00", "Mo-Su 25:00-26:00"})
    void rejectsHoursItCannotRead(String text) {
        restaurantExists();

        InvalidHoursException error = rejected(InvalidHoursException.class, () -> service.updateHours(ID, text));
        assertTrue(error.getMessage().contains("Example: Sa-Th 09:00-23:00; Fr off"));
        assertEquals(HoursSource.UNKNOWN, khamees.getSchedule().getSource());
    }

    // ---------- location ----------

    @Test
    void savesALocationInNablus() {
        restaurantExists();

        Location location = service.updateLocation(ID, 32.2243506, 35.2434154);

        assertEquals(Optional.of(location), khamees.getLocation());
        verify(repository).save(khamees);
    }

    @Test
    void rejectsALocationFarFromNablus() {
        restaurantExists();

        IllegalArgumentException error = rejected(IllegalArgumentException.class,
                () -> service.updateLocation(ID, 31.7683, 35.2137));
        assertTrue(error.getMessage().contains("restaurants must be within 10 km"), error.getMessage());
        assertTrue(khamees.getLocation().isEmpty());
    }

    @Test
    void rejectsImpossibleCoordinates() {
        restaurantExists();

        rejected(IllegalArgumentException.class, () -> service.updateLocation(ID, 95, 35.2));
    }

    // ---------- adding items ----------

    @Test
    void addsAnAvailableItemWithTheNextId() {
        restaurantExists();

        MenuItem item = service.addMenuItem(ID, "  Kunafa  ", new BigDecimal("10"));

        assertEquals(ID + "-3", item.getId());
        assertEquals("Kunafa", item.getName());
        assertTrue(item.isAvailable());
        assertEquals(3, khamees.getMenuItems().size());
        verify(repository).save(khamees);
    }

    @Test
    void rejectsAnItemNameAlreadyOnTheMenu() {
        restaurantExists();

        rejected(IllegalArgumentException.class, () -> service.addMenuItem(ID, "FRIES", new BigDecimal("9")));
        assertEquals(2, khamees.getMenuItems().size());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "Fries | large", "Two\nlines"})
    void rejectsBadItemNames(String name) {
        restaurantExists();

        rejected(IllegalArgumentException.class, () -> service.addMenuItem(ID, name, BigDecimal.TEN));
    }

    @Test
    void rejectsANewItemWithoutAPositivePrice() {
        restaurantExists();

        rejected(IllegalArgumentException.class, () -> service.addMenuItem(ID, "Kunafa", BigDecimal.ZERO));
        assertEquals(2, khamees.getMenuItems().size());
    }

    // ---------- changing items ----------

    @Test
    void changesAPrice() {
        restaurantExists();

        MenuItem item = service.changePrice(ID, ID + "-2", new BigDecimal("9.5"));

        assertEquals(new BigDecimal("9.50"), item.getPrice());
        assertEquals(new BigDecimal("9.50"), khamees.findMenuItem(ID + "-2").orElseThrow().getPrice());
        verify(repository).save(khamees);
    }

    @Test
    void rejectsANegativePriceAndKeepsTheOldOne() {
        restaurantExists();

        rejected(IllegalArgumentException.class, () -> service.changePrice(ID, ID + "-2", new BigDecimal("-3")));
        assertEquals(new BigDecimal("8.00"), khamees.findMenuItem(ID + "-2").orElseThrow().getPrice());
    }

    @Test
    void makesAnItemUnavailableAndAvailableAgain() {
        restaurantExists();

        assertFalse(service.setAvailable(ID, ID + "-1", false).isAvailable());
        assertFalse(khamees.findMenuItem(ID + "-1").orElseThrow().isAvailable());
        assertTrue(service.setAvailable(ID, ID + "-1", true).isAvailable());
    }

    @Test
    void changingAMissingItemIsReported() {
        restaurantExists();

        MenuItemNotFoundException error = rejected(MenuItemNotFoundException.class,
                () -> service.setAvailable(ID, ID + "-9", false));
        assertEquals("Menu item manual-63-9 is not on the menu of manual-63", error.getMessage());
    }

    // ---------- removing items ----------

    @Test
    void removesAnItemForGood() {
        restaurantExists();

        service.removeMenuItem(ID, ID + "-1");

        assertEquals(List.of(ID + "-2"), khamees.getMenuItems().stream().map(MenuItem::getId).toList());
        verify(repository).save(khamees);
    }

    @Test
    void removingAMissingItemIsReported() {
        restaurantExists();

        rejected(MenuItemNotFoundException.class, () -> service.removeMenuItem(ID, ID + "-9"));
    }

    // ---------- item ids ----------

    @Test
    void newIdsNeverReuseTheHighestNumber() {
        khamees.removeMenuItem(ID + "-1");

        assertEquals(ID + "-3", RestaurantManagementService.nextItemId(khamees));
    }

    @Test
    void firstItemOfAnEmptyMenuIsNumberOne() {
        Restaurant empty = new Restaurant("manual-01", "W", WeeklySchedule.unknown(), null, Cuisine.OTHER);

        assertEquals("manual-01-1", RestaurantManagementService.nextItemId(empty));
    }

    @Test
    void idsThatAreNotNumberedAreIgnored() {
        khamees.addMenuItem(new MenuItem(ID + "-special", "Special", BigDecimal.ONE, true));
        khamees.addMenuItem(new MenuItem(ID + "-", "Odd", BigDecimal.ONE, true));
        khamees.addMenuItem(new MenuItem(ID + "-12345678901", "Too long", BigDecimal.ONE, true));
        khamees.addMenuItem(new MenuItem("other-7", "Other", BigDecimal.ONE, true));

        assertEquals(ID + "-3", RestaurantManagementService.nextItemId(khamees));
    }
}
