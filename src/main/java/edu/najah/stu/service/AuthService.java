package edu.najah.stu.service;

import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.User;
import edu.najah.stu.event.AccountEvent;
import edu.najah.stu.event.EventPublisher;
import edu.najah.stu.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Logs users in and creates customer accounts.
 *
 * <p>Wrong usernames and wrong passwords get the same message, so nobody can
 * find out which usernames exist. After {@value #MAX_FAILED_LOGINS} wrong
 * passwords in a row the account is locked for 15 minutes. Both a new
 * account and a lock are published as events, so the user gets an email.
 * The clock comes from a {@link TimeProvider}, so tests can move time forward.</p>
 */
public class AuthService {

    /** Wrong passwords in a row that lock the account. */
    public static final int MAX_FAILED_LOGINS = 3;

    /** How long a locked account stays locked. */
    public static final Duration LOCK_TIME = Duration.ofMinutes(15);

    /** Shortest allowed password. */
    public static final int MIN_PASSWORD_LENGTH = 8;

    static final String WRONG_LOGIN = "Wrong username or password";

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TimeProvider timeProvider;
    private final EventPublisher events;

    /**
     * Creates the service.
     *
     * @param userRepository      where users are stored; must not be null
     * @param passwordHasher      hashes and checks passwords; must not be null
     * @param timeProvider        source of the current time; must not be null
     * @param events              where account events are published; must not be null
     */
    public AuthService(UserRepository userRepository, PasswordHasher passwordHasher, TimeProvider timeProvider,
                       EventPublisher events) {
        if (userRepository == null || passwordHasher == null || timeProvider == null || events == null) {
            throw new IllegalArgumentException("Repository, hasher, time provider and events must not be null");
        }
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.timeProvider = timeProvider;
        this.events = events;
    }

    /**
     * Logs a user in.
     *
     * @param username the username; upper and lower case do not matter
     * @param password the typed password
     * @return the logged-in user
     * @throws AccountLockedException if the account is locked, or this attempt locked it
     * @throws LoginFailedException   if the username or password is wrong
     */
    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new LoginFailedException(WRONG_LOGIN);
        }
        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new LoginFailedException(WRONG_LOGIN));
        LocalDateTime now = timeProvider.now();
        if (user.isLockedAt(now)) {
            throw new AccountLockedException(user.getLockedUntil().orElse(now));
        }
        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw failedLogin(user, now);
        }
        if (user.getFailedLogins() > 0 || user.getLockedUntil().isPresent()) {
            user.clearFailedLogins();
            userRepository.save(user);
        }
        return user;
    }

    /**
     * Creates a customer account. Manager accounts are not created here; they
     * come with the program's data.
     *
     * @param username 3 to 20 letters, digits, {@code _} or {@code .}; must not be taken
     * @param password at least 8 characters with a letter and a digit, not containing the username
     * @param email    where messages will be sent
     * @return the new customer
     * @throws IllegalArgumentException if a value breaks the rules or the username is taken
     */
    public User registerCustomer(String username, String password, String email) {
        String cleanUsername = username == null ? "" : username.trim();
        String cleanEmail = email == null ? "" : email.trim();
        if (!User.isValidUsername(cleanUsername)) {
            throw new IllegalArgumentException("Username must be " + User.MIN_USERNAME_LENGTH + " to "
                    + User.MAX_USERNAME_LENGTH + " letters, digits, '_' or '.'");
        }
        checkPassword(password, cleanUsername);
        if (!User.isValidEmail(cleanEmail)) {
            throw new IllegalArgumentException("'" + cleanEmail + "' is not a valid email address");
        }
        if (userRepository.findByUsername(cleanUsername).isPresent()) {
            throw new IllegalArgumentException("Username '" + cleanUsername + "' is already taken");
        }
        User customer = new User(cleanUsername, passwordHasher.hash(password), Role.CUSTOMER, null, cleanEmail);
        userRepository.save(customer);
        events.publish(AccountEvent.registered(customer));
        return customer;
    }

    private LoginFailedException failedLogin(User user, LocalDateTime now) {
        user.recordFailedLogin();
        if (user.getFailedLogins() >= MAX_FAILED_LOGINS) {
            LocalDateTime until = now.plus(LOCK_TIME).withNano(0);
            user.lockUntil(until);
            userRepository.save(user);
            events.publish(AccountEvent.locked(user, MAX_FAILED_LOGINS, until));
            return new AccountLockedException(until);
        }
        userRepository.save(user);
        return new LoginFailedException(WRONG_LOGIN);
    }

    private static void checkPassword(String password, String username) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new IllegalArgumentException("Password must contain at least one letter and one digit");
        }
        if (password.toLowerCase(Locale.ROOT).contains(username.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Password must not contain the username");
        }
    }
}
