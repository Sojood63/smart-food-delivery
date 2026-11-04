package edu.najah.stu.repository;

import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.User;
import edu.najah.stu.service.Pbkdf2PasswordHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The demo accounts that ship with the program, and copying them on the first run.
 */
class UserDataStoreTest {

    @TempDir
    Path folder;

    @Test
    void bundledAccountsAreSojoodAndOneCustomer() {
        List<User> users = BundledUsers.load();

        assertEquals(List.of("sojood", "customer1"), users.stream().map(User::getUsername).toList());
        assertEquals(Role.MANAGER, users.get(0).getRole());
        assertEquals(Optional.of("manual-63"), users.get(0).getRestaurantId());
        assertEquals(Role.CUSTOMER, users.get(1).getRole());
    }

    @Test
    void bundledPasswordsAreTheOnesInTheReadme() {
        List<User> users = BundledUsers.load();
        Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();

        assertTrue(hasher.matches("Manager123", users.get(0).getPasswordHash()));
        assertTrue(hasher.matches("Customer123", users.get(1).getPasswordHash()));
    }

    @Test
    void sojoodManagesARealRestaurant() {
        List<String> restaurantIds = RestaurantImporter.withDefaultStrategies().importBundledData().stream()
                .map(restaurant -> restaurant.getId()).toList();

        assertTrue(restaurantIds.contains(BundledUsers.load().get(0).getRestaurantId().orElseThrow()));
    }

    @Test
    void firstRunCopiesTheAccountsAndLaterRunsKeepChanges() {
        FileUserRepository first = DataStore.openUsers(folder, BundledUsers::load);
        User sojood = first.findByUsername("sojood").orElseThrow();
        sojood.recordFailedLogin();
        first.save(sojood);

        Supplier<List<User>> mustNotBeUsed = () -> {
            throw new AssertionError("bundled users read again");
        };
        FileUserRepository second = DataStore.openUsers(folder, mustNotBeUsed);

        assertTrue(Files.exists(folder.resolve(DataStore.USERS_FILE)));
        assertEquals(2, second.findAll().size());
        assertEquals(1, second.findByUsername("sojood").orElseThrow().getFailedLogins());
    }

    @Test
    void rejectsMissingArguments() {
        Supplier<List<User>> none = List::of;

        assertThrows(IllegalArgumentException.class, () -> DataStore.openUsers(null, none));
        assertThrows(IllegalArgumentException.class, () -> DataStore.openUsers(folder, null));
    }
}
