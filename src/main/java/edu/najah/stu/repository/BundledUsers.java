package edu.najah.stu.repository;

import edu.najah.stu.domain.User;

import java.util.List;

/**
 * The demo accounts that ship with the program, used on the first run.
 */
public final class BundledUsers {

    /** The bundled accounts file. */
    public static final String USERS_FILE = "users.txt";

    private BundledUsers() {
    }

    /**
     * Reads the bundled demo accounts.
     *
     * @return the users, with their password hashes
     * @throws DataFormatException if the file contains a bad line
     */
    public static List<User> load() {
        return RestaurantImporter.readBundled(USERS_FILE, FileUserRepository.COLUMNS).stream()
                .map(FileUserRepository::fromRow)
                .toList();
    }
}
