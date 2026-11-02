package edu.najah.stu.repository;

import edu.najah.stu.domain.Cuisine;

import java.util.Optional;

/**
 * One restaurant from the team's own list ({@code restaurants.txt}).
 *
 * @param id        unique restaurant id, for example {@code manual-04}
 * @param name      restaurant name
 * @param cuisine   main kind of food
 * @param hoursText opening hours in OpenStreetMap style; empty if not known
 * @param osmId     id of the matching OpenStreetMap place; empty if none
 * @param note      free text for the team, for example what still needs checking
 */
public record ManualEntry(String id, String name, Cuisine cuisine, String hoursText,
                          String osmId, String note) {

    /** Number of fields in a line of {@code restaurants.txt}. */
    public static final int COLUMNS = 6;

    /**
     * Returns the linked OpenStreetMap id, if any.
     *
     * @return the OpenStreetMap id, or empty if the restaurant is not linked
     */
    public Optional<String> linkedOsmId() {
        return osmId.isEmpty() ? Optional.empty() : Optional.of(osmId);
    }

    /**
     * Reads an entry from one line of {@code restaurants.txt}.
     *
     * @param row the line, split into fields
     * @return the entry
     * @throws DataFormatException if the id or name is empty or the cuisine is unknown
     */
    public static ManualEntry fromRow(PipeFile.Row row) {
        String id = row.field(0);
        String name = row.field(1);
        if (id.isEmpty() || name.isEmpty()) {
            throw row.error("id and name must not be empty");
        }
        return new ManualEntry(id, name, cuisine(row), row.field(3), row.field(4), row.field(5));
    }

    private static Cuisine cuisine(PipeFile.Row row) {
        try {
            return Cuisine.valueOf(row.field(2));
        } catch (IllegalArgumentException e) {
            throw row.error("unknown cuisine '" + row.field(2) + "'");
        }
    }
}
