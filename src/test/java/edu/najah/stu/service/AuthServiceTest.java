package edu.najah.stu.service;

import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.User;
import edu.najah.stu.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Logging in, locking after wrong passwords, and creating customer accounts.
 * The repository is a mock and the clock is moved by hand.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final LocalDateTime NOON = LocalDateTime.of(2026, 11, 4, 12, 0);
    private static final String PASSWORD = "Manager123";

    @Mock
    private UserRepository repository;

    private final PasswordHasher hasher = new Pbkdf2PasswordHasher(1000);
    private LocalDateTime now = NOON;
    private AuthService authService;
    private User sojood;

    @BeforeEach
    void setUp() {
        authService = new AuthService(repository, hasher, () -> now);
        sojood = new User("sojood", hasher.hash(PASSWORD), Role.MANAGER, "manual-63", "sojood@example.com");
    }

    private void sojoodExists() {
        when(repository.findByUsername(any())).thenReturn(Optional.of(sojood));
    }

    private void wrongPassword() {
        assertThrows(LoginFailedException.class, () -> authService.login("sojood", "wrong-password1"));
    }

    @Test
    void rejectsMissingParts() {
        TimeProvider clock = () -> now;

        assertThrows(IllegalArgumentException.class, () -> new AuthService(null, hasher, clock));
        assertThrows(IllegalArgumentException.class, () -> new AuthService(repository, null, clock));
        assertThrows(IllegalArgumentException.class, () -> new AuthService(repository, hasher, null));
    }

    // ---------- logging in ----------

    @Test
    void rightPasswordLogsIn() {
        sojoodExists();

        User user = authService.login("sojood", PASSWORD);

        assertSame(sojood, user);
        verify(repository, never()).save(any());
    }

    @Test
    void wrongPasswordIsCountedAndSaved() {
        sojoodExists();

        LoginFailedException error = assertThrows(LoginFailedException.class,
                () -> authService.login("sojood", "Manager124"));

        assertEquals("Wrong username or password", error.getMessage());
        assertEquals(1, sojood.getFailedLogins());
        verify(repository).save(sojood);
    }

    @Test
    void unknownUsernameGetsTheSameMessage() {
        when(repository.findByUsername(any())).thenReturn(Optional.empty());

        LoginFailedException error = assertThrows(LoginFailedException.class,
                () -> authService.login("nobody", PASSWORD));

        assertEquals(AuthService.WRONG_LOGIN, error.getMessage());
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void emptyUsernameIsAWrongLogin(String username) {
        LoginFailedException error = assertThrows(LoginFailedException.class,
                () -> authService.login(username, PASSWORD));

        assertEquals(AuthService.WRONG_LOGIN, error.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void emptyPasswordIsAWrongLogin(String password) {
        assertThrows(LoginFailedException.class, () -> authService.login("sojood", password));
    }

    // ---------- locking ----------

    @Test
    void thirdWrongPasswordLocksForFifteenMinutes() {
        sojoodExists();
        wrongPassword();
        wrongPassword();

        AccountLockedException error = assertThrows(AccountLockedException.class,
                () -> authService.login("sojood", "wrong-password1"));

        assertEquals(NOON.plusMinutes(15), error.getLockedUntil());
        assertEquals("Too many wrong passwords. This account is locked until 12:15", error.getMessage());
        assertTrue(sojood.isLockedAt(NOON.plusMinutes(14)));
    }

    @Test
    void lockedAccountRefusesEvenTheRightPasswordWithoutCheckingIt() {
        PasswordHasher spyHasher = mock(PasswordHasher.class);
        AuthService service = new AuthService(repository, spyHasher, () -> now);
        sojood.lockUntil(NOON.plusMinutes(15));
        sojoodExists();
        now = NOON.plusMinutes(14);

        assertThrows(AccountLockedException.class, () -> service.login("sojood", PASSWORD));

        verify(spyHasher, never()).matches(any(), any());
    }

    @Test
    void lockEndsAfterFifteenMinutesAndIsForgottenOnSuccess() {
        sojoodExists();
        sojood.lockUntil(NOON.plusMinutes(15));
        now = NOON.plusMinutes(15);

        authService.login("sojood", PASSWORD);

        assertEquals(Optional.empty(), sojood.getLockedUntil());
        verify(repository).save(sojood);
    }

    @Test
    void successResetsWrongAttempts() {
        sojoodExists();
        wrongPassword();
        wrongPassword();

        authService.login("sojood", PASSWORD);
        wrongPassword();
        wrongPassword();

        assertEquals(2, sojood.getFailedLogins());
        assertTrue(sojood.getLockedUntil().isEmpty());
    }

    @Test
    void afterALockEndsCountingStartsAgain() {
        sojoodExists();
        sojood.lockUntil(NOON);
        now = NOON.plusMinutes(1);

        wrongPassword();

        assertEquals(1, sojood.getFailedLogins());
        assertFalse(sojood.isLockedAt(now));
    }

    // ---------- creating accounts ----------

    @Test
    void createsACustomerWithAHashedPassword() {
        when(repository.findByUsername(any())).thenReturn(Optional.empty());

        User customer = authService.registerCustomer("  layan_99 ", "Layan2026", " layan@example.com ");

        assertEquals("layan_99", customer.getUsername());
        assertEquals("layan@example.com", customer.getEmail());
        assertEquals(Role.CUSTOMER, customer.getRole());
        assertTrue(hasher.matches("Layan2026", customer.getPasswordHash()));
        verify(repository).save(customer);
    }

    @Test
    void takenUsernameIsRejected() {
        sojoodExists();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> authService.registerCustomer("SOJOOD", "Secret2026", "x@example.com"));

        assertEquals("Username 'SOJOOD' is already taken", error.getMessage());
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ab", "has space", "way_too_long_username_1"})
    void badUsernameIsRejected(String username) {
        assertThrows(IllegalArgumentException.class,
                () -> authService.registerCustomer(username, "Secret2026", "x@example.com"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Short1", "onlyletters", "12345678", "layan123x"})
    void weakPasswordIsRejected(String password) {
        assertThrows(IllegalArgumentException.class,
                () -> authService.registerCustomer("layan", password, "x@example.com"));
    }

    @Test
    void passwordMayNotContainTheUsername() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> authService.registerCustomer("Layan", "mylayan2026", "x@example.com"));

        assertEquals("Password must not contain the username", error.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"layan", "layan@home"})
    void badEmailIsRejected(String email) {
        assertThrows(IllegalArgumentException.class,
                () -> authService.registerCustomer("layan", "Secret2026", email));
    }
}
