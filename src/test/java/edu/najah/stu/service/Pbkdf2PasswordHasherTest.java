package edu.najah.stu.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Pbkdf2PasswordHasherTest {

    private final Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher(1000);

    @Test
    void rightPasswordMatches() {
        String hash = hasher.hash("Manager123");

        assertTrue(hasher.matches("Manager123", hash));
    }

    @Test
    void wrongPasswordDoesNotMatch() {
        String hash = hasher.hash("Manager123");

        assertFalse(hasher.matches("manager123", hash));
        assertFalse(hasher.matches("Manager1234", hash));
    }

    @Test
    void hashNeverContainsThePassword() {
        String hash = hasher.hash("Manager123");

        assertFalse(hash.contains("Manager123"));
        assertTrue(hash.startsWith("pbkdf2$1000$"));
    }

    @Test
    void samePasswordGetsADifferentHashEachTime() {
        assertNotEquals(hasher.hash("Manager123"), hasher.hash("Manager123"));
    }

    @Test
    void hashFromAnotherRoundCountStillMatches() {
        String oldHash = new Pbkdf2PasswordHasher(500).hash("Customer123");

        assertTrue(hasher.matches("Customer123", oldHash));
    }

    @Test
    void defaultHasherUsesTheDefaultRounds() {
        assertTrue(new Pbkdf2PasswordHasher().hash("x").startsWith(
                "pbkdf2$" + Pbkdf2PasswordHasher.DEFAULT_ITERATIONS + "$"));
    }

    @Test
    void rejectsEmptyPasswordAndBadRounds() {
        assertThrows(IllegalArgumentException.class, () -> hasher.hash(""));
        assertThrows(IllegalArgumentException.class, () -> hasher.hash(null));
        assertThrows(IllegalArgumentException.class, () -> new Pbkdf2PasswordHasher(0));
    }

    @Test
    void missingValuesNeverMatch() {
        String hash = hasher.hash("Manager123");

        assertFalse(hasher.matches(null, hash));
        assertFalse(hasher.matches("", hash));
        assertFalse(hasher.matches("Manager123", null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Manager123", "md5$1000$c2FsdA==$aGFzaA==", "pbkdf2$1000$c2FsdA==",
        "pbkdf2$many$c2FsdA==$aGFzaA==", "pbkdf2$0$c2FsdA==$aGFzaA==", "pbkdf2$1000$***$aGFzaA==",
        "pbkdf2$1000$$aGFzaA==", "pbkdf2$1000$c2FsdA==$", "pbkdf2$1000$c2FsdA==$aGFzaA==$extra"})
    void brokenStoredHashesNeverMatch(String storedHash) {
        assertFalse(hasher.matches("Manager123", storedHash));
    }
}
