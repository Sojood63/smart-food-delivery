package edu.najah.stu.repository;

import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.WeeklySchedule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Builds restaurants from the team's list and the OpenStreetMap export.
 *
 * <p>Every restaurant comes from the team's list. Its location comes from the
 * linked OpenStreetMap place, if any, and its menu from the team's menu file. Its hours come from the first
 * {@link HoursStrategy} that knows them; by default the team's hours win over
 * the map, the map wins over an estimate, and with no source at all the hours
 * are unknown.</p>
 */
public class RestaurantImporter {

    /** The team's list, bundled with the program. */
    public static final String RESTAURANTS_FILE = "restaurants.txt";

    /** The OpenStreetMap export, bundled with the program. */
    public static final String OSM_FILE = "osm-places.txt";

    /** The team's menus, bundled with the program. */
    public static final String MENUS_FILE = "menus.txt";

    private static final String DATA_FOLDER = "/data/";

    private final List<HoursStrategy> strategies;

    /**
     * Creates an importer that tries the given strategies in order.
     *
     * @param strategies the hours strategies, most trusted first; must not be empty
     * @throws IllegalArgumentException if the list is null or empty
     */
    public RestaurantImporter(List<HoursStrategy> strategies) {
        if (strategies == null || strategies.isEmpty()) {
            throw new IllegalArgumentException("At least one hours strategy is needed");
        }
        this.strategies = List.copyOf(strategies);
    }

    /**
     * Creates an importer with the default order: team, OpenStreetMap, estimate.
     *
     * @return the importer
     */
    public static RestaurantImporter withDefaultStrategies() {
        return new RestaurantImporter(List.of(
                new ManualHoursStrategy(), new OsmHoursStrategy(), new EstimatedHoursStrategy()));
    }

    /**
     * Reads the data files bundled with the program and builds the restaurants.
     *
     * @return the restaurants in the order of the team's list
     * @throws DataFormatException if a file contains a bad line
     */
    public List<Restaurant> importBundledData() {
        List<ManualEntry> entries = readBundled(RESTAURANTS_FILE, ManualEntry.COLUMNS).stream()
                .map(ManualEntry::fromRow).toList();
        List<OsmPlace> places = readBundled(OSM_FILE, OsmPlace.COLUMNS).stream()
                .map(OsmPlace::fromRow).toList();
        List<MenuRow> menus = readBundled(MENUS_FILE, MenuRow.COLUMNS).stream()
                .map(MenuRow::fromRow).toList();
        return importRestaurants(entries, places, menus);
    }

    /**
     * Builds restaurants from already-read entries and places.
     *
     * @param entries the team's entries
     * @param places  the OpenStreetMap places
     * @return one restaurant per entry, in the same order
     * @throws DataFormatException if ids repeat or an entry links to a missing place
     */
    public List<Restaurant> importRestaurants(List<ManualEntry> entries, List<OsmPlace> places) {
        return importRestaurants(entries, places, List.of());
    }

    /**
     * Builds restaurants with their menus from already-read data.
     *
     * @param entries the team's entries
     * @param places  the OpenStreetMap places
     * @param menus   the team's menu items; item ids are numbered per restaurant
     * @return one restaurant per entry, in the same order
     * @throws DataFormatException if ids repeat, an entry links to a missing place,
     *                             or a menu item belongs to an unknown restaurant
     */
    public List<Restaurant> importRestaurants(List<ManualEntry> entries, List<OsmPlace> places,
                                              List<MenuRow> menus) {
        Map<String, OsmPlace> placesById = indexPlaces(places);
        Set<String> seenIds = new HashSet<>();
        List<Restaurant> restaurants = new ArrayList<>();
        for (ManualEntry entry : entries) {
            if (!seenIds.add(entry.id())) {
                throw new DataFormatException(RESTAURANTS_FILE, "duplicate id " + entry.id());
            }
            restaurants.add(toRestaurant(new ImportCandidate(entry, linkedPlace(entry, placesById))));
        }
        addMenus(restaurants, menus);
        return restaurants;
    }

    private static void addMenus(List<Restaurant> restaurants, List<MenuRow> menus) {
        Map<String, Restaurant> byId = new HashMap<>();
        restaurants.forEach(restaurant -> byId.put(restaurant.getId(), restaurant));
        for (MenuRow row : menus) {
            Restaurant restaurant = byId.get(row.restaurantId());
            if (restaurant == null) {
                throw new DataFormatException(MENUS_FILE, "unknown restaurant id " + row.restaurantId());
            }
            String itemId = restaurant.getId() + "-" + (restaurant.getMenuItems().size() + 1);
            restaurant.addMenuItem(new MenuItem(itemId, row.itemName(), row.price(), row.available()));
        }
    }

    private Restaurant toRestaurant(ImportCandidate candidate) {
        WeeklySchedule schedule = strategies.stream()
                .map(strategy -> strategy.resolve(candidate))
                .flatMap(Optional::stream)
                .findFirst()
                .orElseGet(WeeklySchedule::unknown);
        ManualEntry entry = candidate.entry();
        return new Restaurant(entry.id(), entry.name(), schedule,
                candidate.osmPlace().map(OsmPlace::location).orElse(null), entry.cuisine());
    }

    private static OsmPlace linkedPlace(ManualEntry entry, Map<String, OsmPlace> placesById) {
        return entry.linkedOsmId()
                .map(osmId -> Optional.ofNullable(placesById.get(osmId)).orElseThrow(() ->
                        new DataFormatException(RESTAURANTS_FILE,
                                entry.id() + " links to unknown OpenStreetMap id " + osmId)))
                .orElse(null);
    }

    private static Map<String, OsmPlace> indexPlaces(List<OsmPlace> places) {
        Map<String, OsmPlace> placesById = new HashMap<>();
        for (OsmPlace place : places) {
            if (placesById.putIfAbsent(place.osmId(), place) != null) {
                throw new DataFormatException(OSM_FILE, "duplicate id " + place.osmId());
            }
        }
        return placesById;
    }

    /**
     * Reads one of the data files bundled with the program.
     *
     * @param fileName the file name inside the bundled data folder
     * @param columns  the number of fields every line must have
     * @return the data lines
     */
    static List<PipeFile.Row> readBundled(String fileName, int columns) {
        return withBundledFile(fileName, reader -> PipeFile.read(reader, fileName, columns));
    }

    private static <T> T withBundledFile(String fileName, Function<BufferedReader, T> action) {
        InputStream stream = RestaurantImporter.class.getResourceAsStream(DATA_FOLDER + fileName);
        if (stream == null) {
            throw new IllegalStateException("Missing bundled data file " + fileName);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return action.apply(reader);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot close " + fileName, e);
        }
    }
}
