package edu.najah.stu.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LandmarkTest {

    @Test
    void campusesHaveNamesAndLocations() {
        assertEquals("An-Najah Old Campus", Landmark.OLD_CAMPUS.getDisplayName());
        assertEquals("An-Najah New Campus", Landmark.NEW_CAMPUS.getDisplayName());
        assertEquals(32.227575, Landmark.NEW_CAMPUS.getLocation().getLatitude());
    }

    @Test
    void campusesAreBothInNablus() {
        Location oldCampus = Landmark.OLD_CAMPUS.getLocation();
        Location newCampus = Landmark.NEW_CAMPUS.getLocation();

        assertTrue(oldCampus.distanceKmTo(newCampus) < 5);
    }

    @Test
    void cuisinesHaveDisplayNames() {
        for (Cuisine cuisine : Cuisine.values()) {
            assertTrue(!cuisine.getDisplayName().isBlank());
        }
        assertEquals("Sweets", Cuisine.SWEETS.getDisplayName());
    }
}
