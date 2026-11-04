package edu.najah.stu.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hashes passwords with PBKDF2 (HMAC-SHA256), which is built into Java.
 *
 * <p>A stored hash looks like {@code pbkdf2$120000$salt$hash}: the number of
 * rounds, a random salt and the result, both in Base64. Keeping the number of
 * rounds in the hash means old hashes still work if the number is raised
 * later.</p>
 */
public class Pbkdf2PasswordHasher implements PasswordHasher {

    /** Rounds used by the program; slow enough to make guessing expensive. */
    public static final int DEFAULT_ITERATIONS = 120_000;

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2";
    private static final String SEPARATOR = "$";
    private static final int PARTS = 4;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final int BITS_PER_BYTE = 8;

    private final int iterations;
    private final SecureRandom random = new SecureRandom();

    /**
     * Creates a hasher with {@link #DEFAULT_ITERATIONS} rounds.
     */
    public Pbkdf2PasswordHasher() {
        this(DEFAULT_ITERATIONS);
    }

    /**
     * Creates a hasher with a chosen number of rounds; tests use a small number to run fast.
     *
     * @param iterations rounds for new hashes; must be at least 1
     */
    public Pbkdf2PasswordHasher(int iterations) {
        if (iterations < 1) {
            throw new IllegalArgumentException("Iterations must be at least 1");
        }
        this.iterations = iterations;
    }

    @Override
    public String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        byte[] hash = derive(password, salt, iterations, HASH_BITS);
        Base64.Encoder base64 = Base64.getEncoder();
        return String.join(SEPARATOR, PREFIX, Integer.toString(iterations),
                base64.encodeToString(salt), base64.encodeToString(hash));
    }

    @Override
    public boolean matches(String password, String storedHash) {
        if (password == null || password.isEmpty() || storedHash == null) {
            return false;
        }
        String[] parts = storedHash.split("\\$", -1);
        if (parts.length != PARTS || !parts[0].equals(PREFIX)) {
            return false;
        }
        try {
            int rounds = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            if (rounds < 1 || salt.length == 0 || expected.length == 0) {
                return false;
            }
            byte[] actual = derive(password, salt, rounds, expected.length * BITS_PER_BYTE);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int rounds, int bits) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, rounds, bits);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(ALGORITHM + " is not available", e);
        } finally {
            spec.clearPassword();
        }
    }
}
