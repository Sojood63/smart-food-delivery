package edu.najah.stu.presentation;

import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.User;
import edu.najah.stu.repository.FileUserRepository;
import edu.najah.stu.service.AuthService;
import edu.najah.stu.service.NotificationService;
import edu.najah.stu.service.Pbkdf2PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Drives the login screen with typed answers.
 */
class LoginConsoleTest {

    @TempDir
    Path folder;

    private final Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher(1000);
    private FileUserRepository users;
    private AuthService authService;
    private String printed;

    @BeforeEach
    void setUp() {
        users = new FileUserRepository(folder.resolve("users.txt"));
        users.save(new User("sojood", hasher.hash("Manager123"), Role.MANAGER, "manual-63", "sojood@example.com"));
        authService = new AuthService(users, hasher, () -> LocalDateTime.of(2026, 11, 4, 12, 0),
                mock(NotificationService.class));
    }

    /** Runs the login screen with the given lines typed in; the printed text is kept in {@link #printed}. */
    private Optional<User> type(String... lines) {
        String input = String.join("\n", lines) + "\n";
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Optional<User> user = new LoginConsole(authService, new Scanner(input),
                new PrintStream(output, true, StandardCharsets.UTF_8)).run();
        printed = output.toString(StandardCharsets.UTF_8);
        return user;
    }

    @Test
    void rejectsMissingParts() {
        Scanner in = new Scanner("");
        PrintStream out = System.out;

        assertThrows(IllegalArgumentException.class, () -> new LoginConsole(null, in, out));
        assertThrows(IllegalArgumentException.class, () -> new LoginConsole(authService, null, out));
        assertThrows(IllegalArgumentException.class, () -> new LoginConsole(authService, in, null));
    }

    @Test
    void rightPasswordLogsTheManagerIn() {
        Optional<User> user = type("1", "sojood", "Manager123");

        assertEquals("sojood", user.orElseThrow().getUsername());
        assertTrue(printed.contains("Welcome, sojood (Manager)"));
    }

    @Test
    void wrongPasswordShowsAnErrorAndAsksAgain() {
        Optional<User> user = type("1", "sojood", "nope", "1", "SOJOOD", "Manager123");

        assertTrue(printed.contains("Error: Wrong username or password"));
        assertTrue(user.isPresent());
    }

    @Test
    void threeWrongPasswordsShowTheLock() {
        Optional<User> user = type("1", "sojood", "a", "1", "sojood", "b", "1", "sojood", "c",
                "1", "sojood", "Manager123", "0");

        assertTrue(printed.contains("Error: Too many wrong passwords. This account is locked until 12:15"));
        assertEquals(Optional.empty(), user);
    }

    @Test
    void exitAndEndOfInputReturnNobody() {
        assertEquals(Optional.empty(), type("0"));
        assertEquals(Optional.empty(), type());
    }

    @Test
    void unknownChoiceIsReported() {
        type("7", "0");

        assertTrue(printed.contains("Unknown choice: 7"));
    }

    @Test
    void newCustomerCanRegisterAndThenLogIn() {
        Optional<User> user = type("2", "layan_99", "Layan2026", "layan@example.com",
                "1", "layan_99", "Layan2026");

        assertTrue(printed.contains("Account created for layan_99. You can log in now."));
        assertEquals(Role.CUSTOMER, user.orElseThrow().getRole());
        assertTrue(users.findByUsername("layan_99").isPresent());
    }

    @Test
    void badRegistrationShowsTheReason() {
        type("2", "layan_99", "short", "layan@example.com", "0");

        assertTrue(printed.contains("Error: Password must be at least 8 characters"));
        assertFalse(users.findByUsername("layan_99").isPresent());
    }
}
