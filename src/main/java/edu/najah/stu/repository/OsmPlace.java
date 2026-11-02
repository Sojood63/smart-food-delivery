package edu.najah.stu.repository;

import edu.najah.stu.domain.Location;

/**
 * One food place exported from OpenStreetMap ({@code osm-places.txt}).
 *
 * @param osmId     the OpenStreetMap node or way id
 * @param name      the name on the map
 * @param location  where the place is
 * @param hoursText the {@code opening_hours} tag; empty if the map has none
 * @param cuisine   the {@code cuisine} tag as written on the map; may be empty
 */
public record OsmPlace(String osmId, String name, Location location, String hoursText, String cuisine) {

    /** Number of fields in a line of {@code osm-places.txt}. */
    public static final int COLUMNS = 6;

    /**
     * Reads a place from one line of {@code osm-places.txt}.
     *
     * @param row the line, split into fields
     * @return the place
     * @throws DataFormatException if the id is empty or the coordinates are not valid
     */
    public static OsmPlace fromRow(PipeFile.Row row) {
        if (row.field(0).isEmpty()) {
            throw row.error("OpenStreetMap id must not be empty");
        }
        return new OsmPlace(row.field(0), row.field(1), location(row), row.field(4), row.field(5));
    }

    private static Location location(PipeFile.Row row) {
        try {
            return new Location(Double.parseDouble(row.field(2)), Double.parseDouble(row.field(3)));
        } catch (IllegalArgumentException e) {
            throw row.error("invalid coordinates '" + row.field(2) + ", " + row.field(3) + "'");
        }
    }
}
