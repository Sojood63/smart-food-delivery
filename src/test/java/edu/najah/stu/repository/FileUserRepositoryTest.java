package edu.najah.stu.repository;

import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUserRepositoryTest {

    private static final String HASH = "pbkdf2$1$c2FsdA==$aGFzaA==";

    @TempDir
    Path folder;

    private Path file() {
        return folder.resolve("users.txt");
    }

    private FileUserRepository open() {
        return new FileUserRepository(file());
    }

    private static User manager() {
        return new User("sojood", HASH, Role.MANAGER, "manual-63", "sojood@example.com");
    }

    private void writeLines(String... lines) throws IOException {
        Files.write(file(), List.of(lines), StandardCharsets.UTF_8);
    }

    @Test
    void rejectsMissingPath() {
        assertThrows(IllegalArgumentException.class, () -> new FileUserRepository(null));
    }

    @Test
    void missingFileMeansNoUsers() {
        assertTrue(open().findAll().isEmpty());
        assertFalse(Files.exists(file()));
    }

    @Test
    void userComesBackAfterReopening() {
        open().save(manager());

        User reopened = open().findByUsername("sojood").orElseThrow();

        assertEquals(Role.MANAGER, reopened.getRole());
        assertEquals(Optional.of("manual-63"), reopened.getRestaurantId());
        assertEquals("sojood@example.com", reopened.getEmail());
        assertEquals(HASH, reopened.getPasswordHash());
    }

    @Test
    void lockAndWrongAttemptsComeBackAfterReopening() {
        User user = manager();
        user.lockUntil(LocalDateTime.of(2026, 11, 4, 12, 15));
        user.recordFailedLogin();
        open().save(user);

        User reopened = open().findByUsername("sojood").orElseThrow();

        assertEquals(1, reopened.getFailedLogins());
        assertEquals(Optional.of(LocalDateTime.of(2026, 11, 4, 12, 15)), reopened.getLockedUntil());
    }

    @Test
    void usernamesIgnoreCase() {
        FileUserRepository repository = open();
        repository.save(manager());

        assertTrue(repository.findByUsername("SoJoOd").isPresent());
        assertEquals(Optional.empty(), repository.findByUsername(null));
        assertEquals(Optional.empty(), repository.findByUsername("mira"));
    }

    @Test
    void savingAgainReplacesTheUser() {
        FileUserRepository repository = open();
        repository.save(manager());
        User changed = manager();
        changed.recordFailedLogin();

        repository.save(changed);

        assertEquals(1, open().findAll().size());
        assertEquals(1, open().findAll().get(0).getFailedLogins());
    }

    @Test
    void passwordsAreNeverWrittenOnlyHashes() throws IOException {
        open().save(manager());

        String content = Files.readString(file(), StandardCharsets.UTF_8);

        assertTrue(content.contains(HASH));
        assertTrue(content.startsWith("# username|"));
    }

    @Test
    void rejectsNullUser() {
        FileUserRepository repository = open();
        List<User> withNull = java.util.Arrays.asList(manager(), null);

        assertThrows(IllegalArgumentException.class, () -> repository.saveAll(withNull));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void badRoleIsAnErrorWithTheLineNumber() throws IOException {
        writeLines("# header", "sojood|ADMIN|manual-63|sojood@example.com|0||" + HASH);

        DataFormatException error = assertThrows(DataFormatException.class, this::open);
        assertTrue(error.getMessage().startsWith("users.txt line 2:"), error.getMessage());
    }

    @Test
    void badLockTimeOrCountIsAnError() throws IOException {
        writeLines("customer1|CUSTOMER||c@example.com|0|tomorrow|" + HASH);
        assertThrows(DataFormatException.class, this::open);

        writeLines("customer1|CUSTOMER||c@example.com|many||" + HASH);
        assertThrows(DataFormatException.class, this::open);
    }

    @Test
    void duplicateUsernameIsAnError() throws IOException {
        writeLines("customer1|CUSTOMER||c@example.com|0||" + HASH,
                "Customer1|CUSTOMER||d@example.com|0||" + HASH);

        DataFormatException error = assertThrows(DataFormatException.class, this::open);
        assertTrue(error.getMessage().contains("duplicate username Customer1"));
    }

    @Test
    void wrongNumberOfFieldsIsAnError() throws IOException {
        writeLines("customer1|CUSTOMER||c@example.com|0|");

        assertThrows(DataFormatException.class, this::open);
    }
}
