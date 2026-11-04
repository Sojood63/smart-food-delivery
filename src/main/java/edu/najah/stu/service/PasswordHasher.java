package edu.najah.stu.service;

/**
 * Turns passwords into hashes that are safe to store, and checks a typed
 * password against a stored hash.
 */
public interface PasswordHasher {

    /**
     * Makes a new hash of a password. Hashing the same password twice gives
     * different hashes, because each one uses a new random salt.
     *
     * @param password the password; must not be empty
     * @return the hash, safe to store in a file
     */
    String hash(String password);

    /**
     * Checks a typed password against a stored hash.
     *
     * @param password   the typed password
     * @param storedHash a hash made by {@link #hash(String)}
     * @return {@code true} only if the password matches; {@code false} for any bad input
     */
    boolean matches(String password, String storedHash);
}
