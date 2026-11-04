package edu.najah.stu.repository;

import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.User;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

/**
 * Opens the program's data folder.
 *
 * <p>On the first run the folder is empty, so it is filled once from the
 * bundled data. On later runs the saved files are used as they are, so
 * changes made while the program was running are kept.</p>
 */
public final class DataStore {

    /** File name of the saved restaurants inside the data folder. */
    public static final String RESTAURANTS_FILE = "restaurants.txt";

    /** File name of the saved menu items inside the data folder. */
    public static final String MENU_FILE = "menu_items.txt";

    /** File name of the saved users inside the data folder. */
    public static final String USERS_FILE = "users.txt";

    private DataStore() {
    }

    /**
     * Opens the data folder, filling it from {@code firstRunData} if it holds no restaurants yet.
     *
     * @param folder       the data folder; created if missing
     * @param firstRunData supplies the restaurants for the very first run
     * @return the repository backed by the folder's files
     */
    public static FileRestaurantRepository open(Path folder, Supplier<List<Restaurant>> firstRunData) {
        if (folder == null || firstRunData == null) {
            throw new IllegalArgumentException("Folder and first-run data must not be null");
        }
        Path restaurantsFile = folder.resolve(RESTAURANTS_FILE);
        boolean firstRun = !Files.exists(restaurantsFile);
        FileRestaurantRepository repository = new FileRestaurantRepository(restaurantsFile, folder.resolve(MENU_FILE));
        if (firstRun) {
            repository.saveAll(firstRunData.get());
        }
        return repository;
    }

    /**
     * Opens the users file in the data folder, filling it from {@code firstRunUsers} if it does not exist yet.
     *
     * @param folder        the data folder; created if missing
     * @param firstRunUsers supplies the accounts for the very first run
     * @return the repository backed by the users file
     */
    public static FileUserRepository openUsers(Path folder, Supplier<List<User>> firstRunUsers) {
        if (folder == null || firstRunUsers == null) {
            throw new IllegalArgumentException("Folder and first-run users must not be null");
        }
        Path usersFile = folder.resolve(USERS_FILE);
        boolean firstRun = !Files.exists(usersFile);
        FileUserRepository repository = new FileUserRepository(usersFile);
        if (firstRun) {
            repository.saveAll(firstRunUsers.get());
        }
        return repository;
    }
}
