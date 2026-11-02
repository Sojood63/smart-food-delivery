package edu.najah.stu.repository;

import edu.najah.stu.domain.Restaurant;

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
}
