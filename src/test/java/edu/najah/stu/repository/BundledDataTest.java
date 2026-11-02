package edu.najah.stu.repository;

import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.Restaurant;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks the real data files that ship with the program.
 */
class BundledDataTest {

    private final List<Restaurant> restaurants = RestaurantImporter.withDefaultStrategies().importBundledData();
    private final Map<String, Restaurant> byId = restaurants.stream()
            .collect(Collectors.toMap(Restaurant::getId, Function.identity()));

    private long countWithSource(HoursSource source) {
        return restaurants.stream().filter(restaurant -> restaurant.getSchedule().getSource() == source).count();
    }

    @Test
    void loadsEveryRestaurantFromTheTeamList() {
        assertEquals(66, restaurants.size());
    }

    @Test
    void eighteenRestaurantsHaveAKnownLocation() {
        assertEquals(18, restaurants.stream().filter(restaurant -> restaurant.getLocation().isPresent()).count());
    }

    @Test
    void hoursComeMostlyFromTheTeam() {
        assertEquals(43, countWithSource(HoursSource.MANUAL));
        assertEquals(3, countWithSource(HoursSource.OSM));
        assertEquals(18, countWithSource(HoursSource.ESTIMATED));
        assertEquals(2, countWithSource(HoursSource.UNKNOWN));
    }

    @Test
    void khameesUsesTeamHoursAndMapLocation() {
        Restaurant khamees = byId.get("manual-63");

        assertEquals(HoursSource.MANUAL, khamees.getSchedule().getSource());
        assertEquals(LocalTime.MIDNIGHT, khamees.getSchedule().hoursOn(DayOfWeek.MONDAY).orElseThrow().getClosesAt());
        assertEquals(32.2243506, khamees.getLocation().orElseThrow().getLatitude());
    }

    @Test
    void snackBistnakFallsBackToMapHours() {
        assertEquals(HoursSource.OSM, byId.get("manual-40").getSchedule().getSource());
    }

    @Test
    void nafasIsClosedOnFriday() {
        assertEquals(Optional.empty(), byId.get("manual-06").getSchedule().hoursOn(DayOfWeek.FRIDAY));
    }
}
