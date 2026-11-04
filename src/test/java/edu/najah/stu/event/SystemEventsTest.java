package edu.najah.stu.event;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.HoursParser;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What each event tells the person it concerns.
 */
class SystemEventsTest {

    private static final String HASH = "pbkdf2$1$c2FsdA==$aGFzaA==";

    private final User layan = new User("layan_99", HASH, Role.CUSTOMER, null, "layan@example.com");
    private final User sojood = new User("sojood", HASH, Role.MANAGER, "manual-63", "sojood@example.com");
    private final Restaurant khamees = new Restaurant("manual-63", "Khamees",
            HoursParser.parse("Sa-Th 09:00-23:00; Fr off", HoursSource.MANUAL), null, Cuisine.SHAWARMA);
    private final MenuItem fries = new MenuItem("manual-63-2", "Fries", new BigDecimal("9.5"), true);

    @Test
    void welcomeGoesToTheNewCustomer() {
        AccountEvent welcome = AccountEvent.registered(layan);

        assertSame(layan, welcome.recipient());
        assertEquals("Welcome to Smart Food Delivery", welcome.subject());
        assertTrue(welcome.message().contains("near An-Najah"));
    }

    @Test
    void lockSaysHowManyAttemptsAndWhenToTryAgain() {
        AccountEvent locked = AccountEvent.locked(layan, 3, LocalDateTime.of(2026, 11, 4, 12, 15));

        assertSame(layan, locked.recipient());
        assertEquals("Your Smart Food Delivery account was locked", locked.subject());
        assertTrue(locked.message().contains("3 times in a row"));
        assertTrue(locked.message().contains("after 12:15 on 2026-11-04"), locked.message());
    }

    @Test
    void restaurantEventsGoToTheManagerAndKeepTheRestaurant() {
        List<RestaurantEvent> events = List.of(
                RestaurantEvent.hoursChanged(sojood, khamees),
                RestaurantEvent.locationChanged(sojood, khamees, new Location(32.2243506, 35.2434154)),
                RestaurantEvent.itemAdded(sojood, khamees, fries),
                RestaurantEvent.priceChanged(sojood, khamees, fries, new BigDecimal("8.00")),
                RestaurantEvent.availabilityChanged(sojood, khamees, fries),
                RestaurantEvent.itemRemoved(sojood, khamees, fries));

        for (RestaurantEvent event : events) {
            assertSame(sojood, event.recipient());
            assertSame(khamees, event.restaurant());
            assertTrue(event.message().contains("Khamees") || event.subject().contains("Khamees"),
                    event.subject());
        }
    }

    @Test
    void hoursEventShowsTheNewHours() {
        RestaurantEvent event = RestaurantEvent.hoursChanged(sojood, khamees);

        assertEquals("Opening hours updated: Khamees", event.subject());
        assertTrue(event.message().contains("Mo-Th,Sa-Su 09:00-23:00"), event.message());
    }

    @Test
    void locationEventShowsTheCoordinates() {
        RestaurantEvent event = RestaurantEvent.locationChanged(sojood, khamees, new Location(32.2243506, 35.2434154));

        assertEquals("Location updated: Khamees", event.subject());
        assertTrue(event.message().contains("32.2243506, 35.2434154"));
    }

    @Test
    void menuEventsNameTheItemIdAndPrices() {
        RestaurantEvent added = RestaurantEvent.itemAdded(sojood, khamees, fries);
        assertEquals("New menu item: Fries", added.subject());
        assertTrue(added.message().contains("Fries (manual-63-2)"));
        assertTrue(added.message().contains("9.50 ILS"));

        RestaurantEvent price = RestaurantEvent.priceChanged(sojood, khamees, fries, new BigDecimal("8.00"));
        assertEquals("Price changed: Fries", price.subject());
        assertTrue(price.message().contains("from 8.00 to 9.50 ILS"));

        RestaurantEvent removed = RestaurantEvent.itemRemoved(sojood, khamees, fries);
        assertEquals("Menu item removed: Fries", removed.subject());
        assertTrue(removed.message().contains("for good"));
    }

    @Test
    void availabilityEventSaysWhichWay() {
        RestaurantEvent back = RestaurantEvent.availabilityChanged(sojood, khamees, fries);
        RestaurantEvent soldOut = RestaurantEvent.availabilityChanged(sojood, khamees, fries.withAvailability(false));

        assertEquals("Fries is available again", back.subject());
        assertTrue(back.message().startsWith("Customers can order Fries"));
        assertEquals("Fries is now unavailable", soldOut.subject());
        assertTrue(soldOut.message().startsWith("Customers cannot order Fries"));
    }

    @Test
    void sameChangeMakesEqualEvents() {
        assertEquals(RestaurantEvent.hoursChanged(sojood, khamees), RestaurantEvent.hoursChanged(sojood, khamees));
        assertEquals(AccountEvent.registered(layan), AccountEvent.registered(layan));
    }
}
