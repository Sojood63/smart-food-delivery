package edu.najah.stu.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocationTest {

    private static final double TOLERANCE_KM = 0.01;

    private final Location oldCampus = new Location(32.2189, 35.2400);
    private final Location newCampus = new Location(32.227575, 35.220634);

    // G1
    @Test
    void keepsCoordinates() {
        assertEquals(32.2189, oldCampus.getLatitude());
        assertEquals(35.2400, oldCampus.getLongitude());
    }

    // G2
    @ParameterizedTest
    @ValueSource(strings = {"90.0001", "-90.0001", "91", "-91"})
    void rejectsLatitudeOutOfRange(String latitude) {
        double value = Double.parseDouble(latitude);
        assertThrows(IllegalArgumentException.class, () -> new Location(value, 35));
    }

    // G3
    @ParameterizedTest
    @ValueSource(strings = {"180.0001", "-180.0001", "181", "-181"})
    void rejectsLongitudeOutOfRange(String longitude) {
        double value = Double.parseDouble(longitude);
        assertThrows(IllegalArgumentException.class, () -> new Location(32, value));
    }

    // G4
    @Test
    void rejectsNotANumber() {
        assertThrows(IllegalArgumentException.class, () -> new Location(Double.NaN, 35));
        assertThrows(IllegalArgumentException.class, () -> new Location(32, Double.NaN));
    }

    // G5
    @Test
    void acceptsExactLimits() {
        Location corner = new Location(-90, 180);

        assertEquals(-90, corner.getLatitude());
        assertEquals(180, corner.getLongitude());
    }

    // G6
    @Test
    void distanceToItselfIsZero() {
        assertEquals(0, oldCampus.distanceKmTo(oldCampus), TOLERANCE_KM);
    }

    // G7
    @Test
    void distanceIsTheSameInBothDirections() {
        assertEquals(oldCampus.distanceKmTo(newCampus), newCampus.distanceKmTo(oldCampus), TOLERANCE_KM);
    }

    // G8: one degree of latitude is about 111.19 km everywhere
    @Test
    void oneDegreeOfLatitudeIsAbout111Km() {
        Location start = new Location(32, 35);
        Location oneDegreeNorth = new Location(33, 35);

        assertEquals(111.19, start.distanceKmTo(oneDegreeNorth), TOLERANCE_KM);
    }

    @Test
    void campusesAreAboutTwoKilometresApart() {
        assertEquals(2.0, oldCampus.distanceKmTo(newCampus), 0.1);
    }

    // G9
    @Test
    void rejectsNullOtherLocation() {
        assertThrows(IllegalArgumentException.class, () -> oldCampus.distanceKmTo(null));
    }
}
