package edu.najah.stu.service;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.User;
import edu.najah.stu.domain.WeeklySchedule;
import edu.najah.stu.event.EventPublisher;
import edu.najah.stu.event.HoursChangedEvent;
import edu.najah.stu.event.LocationChangedEvent;
import edu.najah.stu.event.MenuItemAddedEvent;
import edu.najah.stu.event.MenuItemAvailabilityChangedEvent;
import edu.najah.stu.event.MenuItemPriceChangedEvent;
import edu.najah.stu.event.MenuItemRemovedEvent;
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
 * The manager's changes: each good change is saved once and published as an
 * event; each bad one is rejected, and nothing is saved or published.
 * The repository and the event publisher are mocks.
 */
@ExtendWith(MockitoExtension.class)
class RestaurantManagementServiceTest {

    private static final String ID = "manual-63";
    private static final String HASH = "pbkdf2$1$c2FsdA==$aGFzaA==";

    @Mock
    private RestaurantRepository repository;

    @Mock
    private EventPublisher events;

    private RestaurantManagementService service;
    private Restaurant khamees;
    private final User sojood = new User("sojood", HASH, Role.MANAGER, ID, "sojood@example.com");

    @BeforeEach
    void setUp() {
        service = new RestaurantManagementService(repository, events);
        khamees = new Restaurant(ID, "Khamees", WeeklySchedule.unknown(), null, Cuisine.SHAWARMA);
        khamees.addMenuItem(new MenuItem(ID + "-1", "Chicken shawarma", new BigDecimal("12"), true));
        khamees.addMenuItem(new MenuItem(ID + "-2", "Fries", new BigDecimal("8"), true));
    }

    private void restaurantExists() {
        when(repository.findById(any())).thenReturn(Optional.of(khamees));
    }

    /** Checks that a change was rejected, and that nothing was saved or published. */
    private <T extends Throwable> T rejected(Class<T> type, Executable change) {
        T error = assertThrows(type, change);
        verify(repository, never()).save(any());
        verify(events, never()).publish(any());
        return error;
    }

    @Test
    void rejectsMissingParts() {
        assertThrows(IllegalArgumentException.class, () -> new RestaurantManagementService(null, events));
        assertThrows(IllegalArgumentException.class, () -> new RestaurantManagementService(repository, null));
    }

    // ---------- who may manage ----------

    @Test
    void managerGetsTheirOwnRestaurant() {
        restaurantExists();

        assertEquals(khamees, service.getRestaurant(sojood));
    }

    @Test
    void customersAndNobodyCannotManage() {
        User customer = new User("layan", HASH, Role.CUSTOMER, null, "layan@example.com");

        rejected(IllegalArgumentException.class, () -> service.updateHours(customer, "24/7"));
        rejected(IllegalArgumentException.class, () -> service.updateHours(null, "24/7"));
    }

    @Test
    void missingRestaurantIsReported() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        RestaurantNotFoundException error = rejected(RestaurantNotFoundException.class,
                () -> service.updateHours(sojood, "24/7"));
        assertEquals("Restaurant not found: manual-63", error.getMessage());
    }

    // ---------- hours ----------

    @Test
    void savesNewHoursAsTheManagersOwnAndTellsTheManager() {
        restaurantExists();

        WeeklySchedule schedule = service.updateHours(sojood, "Sa-Th 09:00-23:00; Fr off");

        assertEquals(HoursSource.MANUAL, schedule.getSource());
        assertEquals(schedule, khamees.getSchedule());
        assertEquals(Optional.empty(), khamees.getSchedule().hoursOn(DayOfWeek.FRIDAY));
        assertEquals(LocalTime.of(23, 0),
                khamees.getSchedule().hoursOn(DayOfWeek.MONDAY).orElseThrow().getClosesAt());
        verify(repository).save(khamees);
        verify(events).publish(new HoursChangedEvent(sojood, khamees));
    }

    @Test
    void acceptsOpenAllDay() {
        restaurantExists();

        assertTrue(service.updateHours(sojood, "24/7").isOpenAllDay());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"always open", "Sa-Th 9 to 11", "Xx 09:00-23:00", "Mo-Su 25:00-26:00"})
    void rejectsHoursItCannotRead(String text) {
        restaurantExists();

        InvalidHoursException error = rejected(InvalidHoursException.class, () -> service.updateHours(sojood, text));
        assertTrue(error.getMessage().contains("Example: Sa-Th 09:00-23:00; Fr off"));
        assertEquals(HoursSource.UNKNOWN, khamees.getSchedule().getSource());
    }

    // ---------- location ----------

    @Test
    void savesALocationInNablus() {
        restaurantExists();

        Location location = service.updateLocation(sojood, 32.2243506, 35.2434154);

        assertEquals(Optional.of(location), khamees.getLocation());
        verify(repository).save(khamees);
        verify(events).publish(new LocationChangedEvent(sojood, khamees, location));
    }

    @Test
    void rejectsALocationFarFromNablus() {
        restaurantExists();

        IllegalArgumentException error = rejected(IllegalArgumentException.class,
                () -> service.updateLocation(sojood, 31.7683, 35.2137));
        assertTrue(error.getMessage().contains("restaurants must be within 10 km"), error.getMessage());
        assertTrue(khamees.getLocation().isEmpty());
    }

    @Test
    void rejectsImpossibleCoordinates() {
        restaurantExists();

        rejected(IllegalArgumentException.class, () -> service.updateLocation(sojood, 95, 35.2));
    }

    // ---------- adding items ----------

    @Test
    void addsAnAvailableItemWithTheNextId() {
        restaurantExists();

        MenuItem item = service.addMenuItem(sojood, "  Kunafa  ", new BigDecimal("10"));

        assertEquals(ID + "-3", item.getId());
        assertEquals("Kunafa", item.getName());
        assertTrue(item.isAvailable());
        assertEquals(3, khamees.getMenuItems().size());
        verify(repository).save(khamees);
        verify(events).publish(new MenuItemAddedEvent(sojood, khamees, item));
    }

    @Test
    void rejectsAnItemNameAlreadyOnTheMenu() {
        restaurantExists();

        rejected(IllegalArgumentException.class, () -> service.addMenuItem(sojood, "FRIES", new BigDecimal("9")));
        assertEquals(2, khamees.getMenuItems().size());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "Fries | large", "Two\nlines"})
    void rejectsBadItemNames(String name) {
        restaurantExists();

        rejected(IllegalArgumentException.class, () -> service.addMenuItem(sojood, name, BigDecimal.TEN));
    }

    @Test
    void rejectsANewItemWithoutAPositivePrice() {
        restaurantExists();

        rejected(IllegalArgumentException.class, () -> service.addMenuItem(sojood, "Kunafa", BigDecimal.ZERO));
        assertEquals(2, khamees.getMenuItems().size());
    }

    // ---------- changing items ----------

    @Test
    void changesAPriceAndRemembersTheOldOne() {
        restaurantExists();

        MenuItem item = service.changePrice(sojood, ID + "-2", new BigDecimal("9.5"));

        assertEquals(new BigDecimal("9.50"), item.getPrice());
        assertEquals(new BigDecimal("9.50"), khamees.findMenuItem(ID + "-2").orElseThrow().getPrice());
        verify(repository).save(khamees);
        verify(events).publish(new MenuItemPriceChangedEvent(sojood, khamees, item, new BigDecimal("8.00")));
    }

    @Test
    void rejectsANegativePriceAndKeepsTheOldOne() {
        restaurantExists();

        rejected(IllegalArgumentException.class,
                () -> service.changePrice(sojood, ID + "-2", new BigDecimal("-3")));
        assertEquals(new BigDecimal("8.00"), khamees.findMenuItem(ID + "-2").orElseThrow().getPrice());
    }

    @Test
    void makesAnItemUnavailableAndAvailableAgain() {
        restaurantExists();

        MenuItem soldOut = service.setAvailable(sojood, ID + "-1", false);

        assertFalse(soldOut.isAvailable());
        assertFalse(khamees.findMenuItem(ID + "-1").orElseThrow().isAvailable());
        verify(events).publish(new MenuItemAvailabilityChangedEvent(sojood, khamees, soldOut));
        assertTrue(service.setAvailable(sojood, ID + "-1", true).isAvailable());
    }

    @Test
    void changingAMissingItemIsReported() {
        restaurantExists();

        MenuItemNotFoundException error = rejected(MenuItemNotFoundException.class,
                () -> service.setAvailable(sojood, ID + "-9", false));
        assertEquals("Menu item manual-63-9 is not on the menu of manual-63", error.getMessage());
    }

    // ---------- removing items ----------

    @Test
    void removesAnItemForGood() {
        restaurantExists();
        MenuItem chicken = khamees.findMenuItem(ID + "-1").orElseThrow();

        service.removeMenuItem(sojood, ID + "-1");

        assertEquals(List.of(ID + "-2"), khamees.getMenuItems().stream().map(MenuItem::getId).toList());
        verify(repository).save(khamees);
        verify(events).publish(new MenuItemRemovedEvent(sojood, khamees, chicken));
    }

    @Test
    void removingAMissingItemIsReported() {
        restaurantExists();

        rejected(MenuItemNotFoundException.class, () -> service.removeMenuItem(sojood, ID + "-9"));
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
