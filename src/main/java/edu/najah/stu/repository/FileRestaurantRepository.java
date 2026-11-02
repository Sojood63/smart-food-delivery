package edu.najah.stu.repository;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.HoursFormatter;
import edu.najah.stu.domain.HoursParser;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.WeeklySchedule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Keeps restaurants and their menus in two text files, so changes survive
 * a restart of the program.
 *
 * <pre>
 * restaurants file: id|name|cuisine|hours|hoursSource|latitude|longitude
 * menu file:        id|restaurantId|name|price|available
 * </pre>
 *
 * <p>Both files are read once when the repository is created and rewritten
 * on every save. A save writes to a temporary file first and then replaces
 * the old file, so a crash in the middle of writing cannot leave a half
 * written file behind.</p>
 */
public class FileRestaurantRepository implements RestaurantRepository {

    private static final int RESTAURANT_COLUMNS = 7;
    private static final int MENU_COLUMNS = 5;
    private static final String RESTAURANT_HEADER = "# id|name|cuisine|hours|hoursSource|latitude|longitude";
    private static final String MENU_HEADER = "# id|restaurantId|name|price|available";

    private final Path restaurantsFile;
    private final Path menuFile;
    private final Map<String, Restaurant> restaurants = new LinkedHashMap<>();

    /**
     * Opens the repository and reads both files. Missing files mean an empty repository.
     *
     * @param restaurantsFile where restaurants are kept; must not be null
     * @param menuFile        where menu items are kept; must not be null
     * @throws DataFormatException  if a file contains a bad line
     * @throws UncheckedIOException if a file cannot be read
     */
    public FileRestaurantRepository(Path restaurantsFile, Path menuFile) {
        if (restaurantsFile == null || menuFile == null) {
            throw new IllegalArgumentException("File paths must not be null");
        }
        this.restaurantsFile = restaurantsFile;
        this.menuFile = menuFile;
        readRows(restaurantsFile, RESTAURANT_COLUMNS).forEach(this::loadRestaurant);
        readRows(menuFile, MENU_COLUMNS).forEach(this::loadMenuItem);
    }

    @Override
    public void save(Restaurant restaurant) {
        checkStorable(restaurant);
        saveAll(List.of(restaurant));
    }

    /**
     * Saves several restaurants and writes the files once.
     *
     * @param newRestaurants the restaurants to save; none may be null
     * @throws IllegalArgumentException if a restaurant is null or a text contains {@code |} or a line break
     */
    public void saveAll(Collection<Restaurant> newRestaurants) {
        for (Restaurant restaurant : newRestaurants) {
            checkStorable(restaurant);
        }
        newRestaurants.forEach(restaurant -> restaurants.put(restaurant.getId(), restaurant));
        writeFiles();
    }

    @Override
    public Optional<Restaurant> findById(String id) {
        return Optional.ofNullable(restaurants.get(id));
    }

    @Override
    public List<Restaurant> findAll() {
        return List.copyOf(restaurants.values());
    }

    private static void checkStorable(Restaurant restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant must not be null");
        }
        checkText(restaurant.getId());
        checkText(restaurant.getName());
        restaurant.getMenuItems().forEach(item -> {
            checkText(item.getId());
            checkText(item.getName());
        });
    }

    private static void checkText(String text) {
        if (text.contains("|") || text.contains("\n") || text.contains("\r")) {
            throw new IllegalArgumentException("Text must not contain '|' or line breaks: " + text);
        }
    }

    // ---------- reading ----------

    private static List<PipeFile.Row> readRows(Path file, int columns) {
        if (!Files.exists(file)) {
            return List.of();
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return PipeFile.read(reader, file.getFileName().toString(), columns);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
    }

    private void loadRestaurant(PipeFile.Row row) {
        String id = row.field(0);
        if (restaurants.containsKey(id)) {
            throw row.error("duplicate restaurant id " + id);
        }
        try {
            Cuisine cuisine = Cuisine.valueOf(row.field(2));
            WeeklySchedule schedule = schedule(row, HoursSource.valueOf(row.field(4)));
            restaurants.put(id, new Restaurant(id, row.field(1), schedule, location(row), cuisine));
        } catch (IllegalArgumentException e) {
            throw row.error(e.getMessage());
        }
    }

    private static WeeklySchedule schedule(PipeFile.Row row, HoursSource source) {
        if (source == HoursSource.UNKNOWN) {
            return WeeklySchedule.unknown();
        }
        WeeklySchedule schedule = HoursParser.parse(row.field(3), source);
        if (schedule.getSource() == HoursSource.UNKNOWN) {
            throw row.error("cannot read hours '" + row.field(3) + "'");
        }
        return schedule;
    }

    private static Location location(PipeFile.Row row) {
        if (row.field(5).isEmpty() && row.field(6).isEmpty()) {
            return null;
        }
        return new Location(Double.parseDouble(row.field(5)), Double.parseDouble(row.field(6)));
    }

    private void loadMenuItem(PipeFile.Row row) {
        Restaurant restaurant = restaurants.get(row.field(1));
        if (restaurant == null) {
            throw row.error("menu item for unknown restaurant " + row.field(1));
        }
        try {
            restaurant.addMenuItem(new MenuItem(row.field(0), row.field(2),
                    new BigDecimal(row.field(3)), MenuRow.yesOrNo(row, row.field(4))));
        } catch (IllegalArgumentException e) {
            throw row.error(e.getMessage());
        }
    }

    // ---------- writing ----------

    private void writeFiles() {
        List<String> restaurantLines = new ArrayList<>(List.of(RESTAURANT_HEADER));
        List<String> menuLines = new ArrayList<>(List.of(MENU_HEADER));
        for (Restaurant restaurant : restaurants.values()) {
            restaurantLines.add(restaurantLine(restaurant));
            restaurant.getMenuItems().forEach(item -> menuLines.add(menuLine(restaurant, item)));
        }
        writeSafely(restaurantsFile, restaurantLines);
        writeSafely(menuFile, menuLines);
    }

    private static String restaurantLine(Restaurant restaurant) {
        WeeklySchedule schedule = restaurant.getSchedule();
        String latitude = restaurant.getLocation().map(location -> Double.toString(location.getLatitude())).orElse("");
        String longitude = restaurant.getLocation().map(location -> Double.toString(location.getLongitude())).orElse("");
        return String.join("|", restaurant.getId(), restaurant.getName(), restaurant.getCuisine().name(),
                HoursFormatter.format(schedule), schedule.getSource().name(), latitude, longitude);
    }

    private static String menuLine(Restaurant restaurant, MenuItem item) {
        return String.join("|", item.getId(), restaurant.getId(), item.getName(),
                item.getPrice().toPlainString(), item.isAvailable() ? "yes" : "no");
    }

    /** Writes to a temporary file next to the target, then moves it over the target. */
    private static void writeSafely(Path file, List<String> lines) {
        try {
            Path folder = file.toAbsolutePath().getParent();
            Files.createDirectories(folder);
            Path temporary = Files.createTempFile(folder, file.getFileName().toString(), ".tmp");
            Files.write(temporary, lines, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write " + file, e);
        }
    }
}
