package edu.najah.stu.repository;

import edu.najah.stu.domain.Cuisine;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Reading single lines of {@code restaurants.txt} and {@code osm-places.txt}.
 */
class DataLineTest {

    private static PipeFile.Row row(String line) {
        return PipeFile.read(new BufferedReader(new StringReader(line)), "data.txt", 6).get(0);
    }

    @Test
    void readsATeamEntry() {
        ManualEntry entry = ManualEntry.fromRow(row(
                "manual-04|Jaltah|BURGER|Mo-Th,Sa-Su 11:30-01:00; Fr 12:30-02:00|9897934617|"));

        assertEquals("manual-04", entry.id());
        assertEquals("Jaltah", entry.name());
        assertEquals(Cuisine.BURGER, entry.cuisine());
        assertEquals("Mo-Th,Sa-Su 11:30-01:00; Fr 12:30-02:00", entry.hoursText());
        assertEquals(Optional.of("9897934617"), entry.linkedOsmId());
        assertEquals("", entry.note());
    }

    @Test
    void teamEntryWithoutMapLinkOrHours() {
        ManualEntry entry = ManualEntry.fromRow(row("manual-05|FIRE Shawarma|SHAWARMA|||hours not known"));

        assertEquals(Optional.empty(), entry.linkedOsmId());
        assertEquals("", entry.hoursText());
        assertEquals("hours not known", entry.note());
    }

    @Test
    void rejectsUnknownCuisine() {
        PipeFile.Row line = row("manual-01|W Restaurant|FASTFOOD|||");

        DataFormatException error = assertThrows(DataFormatException.class, () -> ManualEntry.fromRow(line));
        assertEquals("data.txt line 1: unknown cuisine 'FASTFOOD'", error.getMessage());
    }

    @Test
    void rejectsMissingIdOrName() {
        PipeFile.Row noId = row("|W Restaurant|OTHER|||");
        PipeFile.Row noName = row("manual-01||OTHER|||");

        assertThrows(DataFormatException.class, () -> ManualEntry.fromRow(noId));
        assertThrows(DataFormatException.class, () -> ManualEntry.fromRow(noName));
    }

    @Test
    void readsAMapPlace() {
        OsmPlace place = OsmPlace.fromRow(row("5347793623|Khamees|32.2243506|35.2434154|Mo-Su 07:00-01:00|"));

        assertEquals("5347793623", place.osmId());
        assertEquals("Khamees", place.name());
        assertEquals(32.2243506, place.location().getLatitude());
        assertEquals(35.2434154, place.location().getLongitude());
        assertEquals("Mo-Su 07:00-01:00", place.hoursText());
        assertEquals("", place.cuisine());
    }

    @Test
    void rejectsCoordinatesThatAreNotNumbers() {
        PipeFile.Row line = row("1|Somewhere|north|35.2|| ");

        DataFormatException error = assertThrows(DataFormatException.class, () -> OsmPlace.fromRow(line));
        assertEquals("data.txt line 1: invalid coordinates 'north, 35.2'", error.getMessage());
    }

    @Test
    void rejectsCoordinatesOutOfRange() {
        PipeFile.Row line = row("1|Somewhere|132.2|35.2||");

        assertThrows(DataFormatException.class, () -> OsmPlace.fromRow(line));
    }

    @Test
    void rejectsMapPlaceWithoutId() {
        PipeFile.Row line = row("|Somewhere|32.2|35.2||");

        assertThrows(DataFormatException.class, () -> OsmPlace.fromRow(line));
    }
}
