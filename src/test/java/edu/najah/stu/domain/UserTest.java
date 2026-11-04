package edu.najah.stu.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    private static final String HASH = "pbkdf2$1$c2FsdA==$aGFzaA==";
    private static final LocalDateTime NOON = LocalDateTime.of(2026, 11, 4, 12, 0);

    private static User customer() {
        return new User("customer1", HASH, Role.CUSTOMER, null, "customer1@example.com");
    }

    @Test
    void managerKnowsTheirRestaurant() {
        User manager = new User("sojood", HASH, Role.MANAGER, "manual-63", "sojood@example.com");

        assertEquals(Optional.of("manual-63"), manager.getRestaurantId());
        assertEquals(Role.MANAGER, manager.getRole());
        assertEquals("Manager", manager.getRole().getDisplayName());
    }

    @Test
    void customerHasNoRestaurant() {
        User customer = customer();

        assertEquals(Optional.empty(), customer.getRestaurantId());
        assertEquals("customer1", customer.getUsername());
        assertEquals(HASH, customer.getPasswordHash());
        assertEquals("customer1@example.com", customer.getEmail());
    }

    @Test
    void managerWithoutRestaurantIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("sojood", HASH, Role.MANAGER, null, "sojood@example.com"));
        assertThrows(IllegalArgumentException.class,
                () -> new User("sojood", HASH, Role.MANAGER, " ", "sojood@example.com"));
    }

    @Test
    void customerWithRestaurantIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("ali", HASH, Role.CUSTOMER, "manual-63", "ali@example.com"));
    }

    @Test
    void missingHashOrRoleIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new User("ali", "", Role.CUSTOMER, null, "a@b.co"));
        assertThrows(IllegalArgumentException.class, () -> new User("ali", null, Role.CUSTOMER, null, "a@b.co"));
        assertThrows(IllegalArgumentException.class, () -> new User("ali", HASH, null, null, "a@b.co"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ali", "sojood_63", "s.sard", "Mira2026", "abcdefghijklmnopqrst", "سجود"})
    void acceptsGoodUsernames(String username) {
        assertTrue(User.isValidUsername(username));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ab", "abcdefghijklmnopqrstu", "two words", "a|b", "ali@home", "ali-1"})
    void rejectsBadUsernames(String username) {
        assertFalse(User.isValidUsername(username));
    }

    @ParameterizedTest
    @ValueSource(strings = {"s12323117@stu.najah.edu", "a@b.co", "first.last@example.com"})
    void acceptsGoodEmails(String email) {
        assertTrue(User.isValidEmail(email));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"plainaddress", "@example.com", "a@@b.com", "a@b@c.com", "a@nodot", "a@.com",
        "a@b.", "a b@c.com", "a|b@c.com"})
    void rejectsBadEmails(String email) {
        assertFalse(User.isValidEmail(email));
    }

    @Test
    void badEmailIsRejectedByTheConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new User("ali", HASH, Role.CUSTOMER, null, "ali"));
    }

    @Test
    void newUserIsNotLocked() {
        User user = customer();

        assertEquals(0, user.getFailedLogins());
        assertEquals(Optional.empty(), user.getLockedUntil());
        assertFalse(user.isLockedAt(NOON));
    }

    @Test
    void lockLastsUntilItsEndAndResetsTheCount() {
        User user = customer();
        user.recordFailedLogin();
        user.recordFailedLogin();

        user.lockUntil(NOON.plusMinutes(15));

        assertEquals(0, user.getFailedLogins());
        assertTrue(user.isLockedAt(NOON));
        assertTrue(user.isLockedAt(NOON.plusMinutes(14)));
        assertFalse(user.isLockedAt(NOON.plusMinutes(15)));
        assertThrows(IllegalArgumentException.class, () -> user.lockUntil(null));
    }

    @Test
    void clearingForgetsAttemptsAndLock() {
        User user = customer();
        user.recordFailedLogin();
        user.lockUntil(NOON);

        user.clearFailedLogins();

        assertEquals(0, user.getFailedLogins());
        assertEquals(Optional.empty(), user.getLockedUntil());
    }

    @Test
    void restoresSavedLoginState() {
        User user = customer();

        user.restoreLoginState(2, NOON);

        assertEquals(2, user.getFailedLogins());
        assertEquals(Optional.of(NOON), user.getLockedUntil());
        assertThrows(IllegalArgumentException.class, () -> user.restoreLoginState(-1, null));
    }
}
