package edu.najah.stu.repository;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.WeeklySchedule;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The three ways of finding opening hours: team, map, estimate.
 */
class HoursStrategyTest {

    private static final Location NABLUS = new Location(32.22, 35.24);

    private static ImportCandidate candidate(Cuisine cuisine, String teamHours, String mapHours) {
        ManualEntry entry = new ManualEntry("manual-01", "Test place", cuisine, teamHours,
                mapHours == null ? "" : "1", "");
        OsmPlace place = mapHours == null ? null : new OsmPlace("1", "Test place", NABLUS, mapHours, "");
        return new ImportCandidate(entry, place);
    }

    private static Optional<HoursSource> sourceOf(Optional<WeeklySchedule> schedule) {
        return schedule.map(WeeklySchedule::getSource);
    }

    @Test
    void teamStrategyUsesTeamHours() {
        Optional<WeeklySchedule> schedule = new ManualHoursStrategy()
                .resolve(candidate(Cuisine.PIZZA, "Mo-Su 09:00-00:00", "Mo-Su 11:00-00:00"));

        assertEquals(Optional.of(HoursSource.MANUAL), sourceOf(schedule));
        assertEquals(LocalTime.of(9, 0), schedule.orElseThrow().hoursOn(DayOfWeek.MONDAY).orElseThrow().getOpensAt());
    }

    @Test
    void teamStrategyGivesUpWhenTeamHoursAreEmptyOrBroken() {
        ManualHoursStrategy strategy = new ManualHoursStrategy();

        assertEquals(Optional.empty(), strategy.resolve(candidate(Cuisine.PIZZA, "", null)));
        assertEquals(Optional.empty(), strategy.resolve(candidate(Cuisine.PIZZA, "always open", null)));
    }

    @Test
    void mapStrategyUsesMapHours() {
        Optional<WeeklySchedule> schedule = new OsmHoursStrategy()
                .resolve(candidate(Cuisine.PIZZA, "", "Sa-Th 07:00-22:00; Fr 09:00-11:35"));

        assertEquals(Optional.of(HoursSource.OSM), sourceOf(schedule));
    }

    @Test
    void mapStrategyGivesUpWithoutLinkOrUsableMapHours() {
        OsmHoursStrategy strategy = new OsmHoursStrategy();

        assertEquals(Optional.empty(), strategy.resolve(candidate(Cuisine.PIZZA, "", null)));
        assertEquals(Optional.empty(), strategy.resolve(candidate(Cuisine.PIZZA, "", "")));
        assertEquals(Optional.empty(), strategy.resolve(candidate(Cuisine.PIZZA, "", "Sa-Th 11:00-23:00; Fr")));
    }

    @Test
    void estimateStrategyGuessesFromCuisine() {
        Optional<WeeklySchedule> schedule = new EstimatedHoursStrategy()
                .resolve(candidate(Cuisine.SHAWARMA, "", null));

        assertEquals(Optional.of(HoursSource.ESTIMATED), sourceOf(schedule));
        assertTrue(schedule.orElseThrow().hoursOn(DayOfWeek.FRIDAY).orElseThrow().closesOvernight());
    }

    @Test
    void estimateStrategyHasAGuessForEveryCuisineExceptOther() {
        EstimatedHoursStrategy strategy = new EstimatedHoursStrategy();

        for (Cuisine cuisine : Cuisine.values()) {
            Optional<WeeklySchedule> schedule = strategy.resolve(candidate(cuisine, "", null));
            assertEquals(cuisine != Cuisine.OTHER, schedule.isPresent(), cuisine.name());
        }
    }
}
