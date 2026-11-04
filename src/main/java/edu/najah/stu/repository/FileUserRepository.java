package edu.najah.stu.repository;

import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Keeps users in a text file, so accounts and locks survive a restart.
 *
 * <pre>
 * username|role|restaurantId|email|failedLogins|lockedUntil|passwordHash
 * </pre>
 *
 * <p>Only password hashes are stored, never passwords. The file is read once
 * when the repository is created and rewritten safely on every save.</p>
 */
public class FileUserRepository implements UserRepository {

    /** Number of fields on every line. */
    static final int COLUMNS = 7;

    private static final String HEADER = "# username|role|restaurantId|email|failedLogins|lockedUntil|passwordHash";

    private final Path file;
    private final Map<String, User> users = new LinkedHashMap<>();

    /**
     * Opens the repository and reads the file. A missing file means no users yet.
     *
     * @param file where users are kept; must not be null
     * @throws DataFormatException  if the file contains a bad line
     * @throws UncheckedIOException if the file cannot be read
     */
    public FileUserRepository(Path file) {
        if (file == null) {
            throw new IllegalArgumentException("File path must not be null");
        }
        this.file = file;
        for (PipeFile.Row row : readRows(file)) {
            User user = fromRow(row);
            if (users.putIfAbsent(key(user.getUsername()), user) != null) {
                throw row.error("duplicate username " + user.getUsername());
            }
        }
    }

    @Override
    public void save(User user) {
        saveAll(List.of(user));
    }

    /**
     * Saves several users and writes the file once.
     *
     * @param newUsers the users to save; none may be null
     */
    public void saveAll(Collection<User> newUsers) {
        for (User user : newUsers) {
            if (user == null) {
                throw new IllegalArgumentException("User must not be null");
            }
        }
        newUsers.forEach(user -> users.put(key(user.getUsername()), user));
        write();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return username == null ? Optional.empty() : Optional.ofNullable(users.get(key(username)));
    }

    @Override
    public List<User> findAll() {
        return List.copyOf(users.values());
    }

    private static String key(String username) {
        return username.toLowerCase(Locale.ROOT);
    }

    /**
     * Builds a user from one line of a users file.
     *
     * @param row the line
     * @return the user with its saved login state
     * @throws DataFormatException if a field is invalid
     */
    static User fromRow(PipeFile.Row row) {
        try {
            String restaurantId = row.field(2).isEmpty() ? null : row.field(2);
            User user = new User(row.field(0), row.field(6), Role.valueOf(row.field(1)), restaurantId, row.field(3));
            LocalDateTime lockedUntil = row.field(5).isEmpty() ? null : LocalDateTime.parse(row.field(5));
            user.restoreLoginState(Integer.parseInt(row.field(4)), lockedUntil);
            return user;
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw row.error(e.getMessage());
        }
    }

    private static List<PipeFile.Row> readRows(Path file) {
        if (!Files.exists(file)) {
            return List.of();
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return PipeFile.read(reader, file.getFileName().toString(), COLUMNS);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
    }

    private void write() {
        List<String> lines = new ArrayList<>(List.of(HEADER));
        for (User user : users.values()) {
            lines.add(String.join("|", user.getUsername(), user.getRole().name(),
                    user.getRestaurantId().orElse(""), user.getEmail(), Integer.toString(user.getFailedLogins()),
                    user.getLockedUntil().map(LocalDateTime::toString).orElse(""), user.getPasswordHash()));
        }
        SafeFile.write(file, lines);
    }
}
