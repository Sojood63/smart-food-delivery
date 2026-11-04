package edu.najah.stu.domain;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * A person who can log in: a customer, or the manager of one restaurant.
 *
 * <p>The password itself is never kept, only its hash. The user also
 * remembers wrong login attempts, so the account can be locked for a while
 * after too many of them.</p>
 */
public class User {

    /** Shortest allowed username. */
    public static final int MIN_USERNAME_LENGTH = 3;

    /** Longest allowed username. */
    public static final int MAX_USERNAME_LENGTH = 20;

    private final String username;
    private final String passwordHash;
    private final Role role;
    private final String restaurantId;
    private final String email;
    private int failedLogins;
    private LocalDateTime lockedUntil;

    /**
     * Creates a user with no wrong login attempts.
     *
     * @param username     3 to 20 letters, digits, {@code _} or {@code .}
     * @param passwordHash the stored hash of the password; must not be empty
     * @param role         what the user may do; must not be null
     * @param restaurantId the restaurant a manager runs; must be null for a customer
     * @param email        where messages are sent; must look like an email address
     * @throws IllegalArgumentException if any value breaks these rules
     */
    public User(String username, String passwordHash, Role role, String restaurantId, String email) {
        if (!isValidUsername(username)) {
            throw new IllegalArgumentException("Username must be " + MIN_USERNAME_LENGTH + " to "
                    + MAX_USERNAME_LENGTH + " letters, digits, '_' or '.'");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be empty");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }
        boolean hasRestaurant = restaurantId != null && !restaurantId.isBlank();
        if (role == Role.MANAGER && !hasRestaurant) {
            throw new IllegalArgumentException("A manager must have a restaurant");
        }
        if (role == Role.CUSTOMER && restaurantId != null) {
            throw new IllegalArgumentException("A customer cannot have a restaurant");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("'" + email + "' is not a valid email address");
        }
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.restaurantId = restaurantId;
        this.email = email;
    }

    /**
     * Tells whether a text can be used as a username.
     *
     * @param username the text to check
     * @return {@code true} if it has 3 to 20 characters, each a letter, digit, {@code _} or {@code .}
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.length() < MIN_USERNAME_LENGTH
                || username.length() > MAX_USERNAME_LENGTH) {
            return false;
        }
        return username.chars().allMatch(c -> Character.isLetterOrDigit(c) || c == '_' || c == '.');
    }

    /**
     * Tells whether a text looks like an email address: something, one {@code @},
     * then a domain with a dot, and no spaces.
     *
     * @param email the text to check
     * @return {@code true} if it looks like an email address
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty() || email.chars().anyMatch(Character::isWhitespace)
                || email.contains("|")) {
            return false;
        }
        int at = email.indexOf('@');
        if (at <= 0 || at != email.lastIndexOf('@')) {
            return false;
        }
        String domain = email.substring(at + 1);
        int dot = domain.lastIndexOf('.');
        return dot > 0 && dot < domain.length() - 1;
    }

    /**
     * Returns the username.
     *
     * @return the username, as it was typed when the account was made
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the stored password hash.
     *
     * @return the hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Returns what the user may do.
     *
     * @return the role
     */
    public Role getRole() {
        return role;
    }

    /**
     * Returns the restaurant this user manages.
     *
     * @return the restaurant id, or empty for a customer
     */
    public Optional<String> getRestaurantId() {
        return Optional.ofNullable(restaurantId);
    }

    /**
     * Returns the email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the number of wrong login attempts since the last success or lock.
     *
     * @return the count, zero or more
     */
    public int getFailedLogins() {
        return failedLogins;
    }

    /**
     * Returns when the last lock ends or ended.
     *
     * @return the end of the lock, or empty if the account was never locked since the last success
     */
    public Optional<LocalDateTime> getLockedUntil() {
        return Optional.ofNullable(lockedUntil);
    }

    /**
     * Tells whether the account is locked at a moment.
     *
     * @param moment the moment to check; must not be null
     * @return {@code true} if a lock is still running at that moment
     */
    public boolean isLockedAt(LocalDateTime moment) {
        return lockedUntil != null && moment.isBefore(lockedUntil);
    }

    /**
     * Counts one more wrong login attempt.
     */
    public void recordFailedLogin() {
        failedLogins++;
    }

    /**
     * Locks the account and starts counting wrong attempts from zero again.
     *
     * @param until when the lock ends; must not be null
     */
    public void lockUntil(LocalDateTime until) {
        if (until == null) {
            throw new IllegalArgumentException("Lock end must not be null");
        }
        lockedUntil = until;
        failedLogins = 0;
    }

    /**
     * Forgets wrong attempts and any lock, after a successful login.
     */
    public void clearFailedLogins() {
        failedLogins = 0;
        lockedUntil = null;
    }

    /**
     * Restores the login state read from storage.
     *
     * @param savedFailedLogins wrong attempts so far; must not be negative
     * @param savedLockedUntil  end of the lock, or null if there is none
     * @throws IllegalArgumentException if the count is negative
     */
    public void restoreLoginState(int savedFailedLogins, LocalDateTime savedLockedUntil) {
        if (savedFailedLogins < 0) {
            throw new IllegalArgumentException("Failed logins must not be negative");
        }
        failedLogins = savedFailedLogins;
        lockedUntil = savedLockedUntil;
    }
}
