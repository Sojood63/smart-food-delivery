package edu.najah.stu.repository;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.WeeklySchedule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantImporterTest {

    private static final Location KHAMEES_LOCATION = new Location(32.2243506, 35.2434154);

    private final RestaurantImporter importer = RestaurantImporter.withDefaultStrategies();

    @Mock
    private HoursStrategy firstStrategy;

    @Mock
    private HoursStrategy secondStrategy;

    private static ManualEntry entry(String id, Cuisine cuisine, String hours, String osmId) {
        return new ManualEntry(id, "Restaurant " + id, cuisine, hours, osmId, "");
    }

    private static OsmPlace place(String osmId, String hours) {
        return new OsmPlace(osmId, "Map place " + osmId, KHAMEES_LOCATION, hours, "");
    }

    private Restaurant importOne(ManualEntry entry, OsmPlace... places) {
        return importer.importRestaurants(List.of(entry), List.of(places)).get(0);
    }

    private static LocalTime closingOnMonday(Restaurant restaurant) {
        return restaurant.getSchedule().hoursOn(DayOfWeek.MONDAY).orElseThrow().getClosesAt();
    }

    @Test
    void teamHoursWinOverMapHours() {
        Restaurant khamees = importOne(entry("manual-63", Cuisine.SHAWARMA, "Mo-Su 07:00-00:00", "5347793623"),
                place("5347793623", "Mo-Su 07:00-01:00"));

        assertEquals(HoursSource.MANUAL, khamees.getSchedule().getSource());
        assertEquals(LocalTime.MIDNIGHT, closingOnMonday(khamees));
    }

    @Test
    void mapHoursAreUsedWhenTeamHoursAreMissing() {
        Restaurant snack = importOne(entry("manual-40", Cuisine.PIZZA, "", "6946231785"),
                place("6946231785", "Sa-Th 07:00-22:00; Fr 09:00-11:35"));

        assertEquals(HoursSource.OSM, snack.getSchedule().getSource());
        assertEquals(LocalTime.of(22, 0), closingOnMonday(snack));
    }

    @Test
    void hoursAreEstimatedWhenNoSourceKnowsThem() {
        Restaurant fire = importOne(entry("manual-05", Cuisine.SHAWARMA, "", ""));

        assertEquals(HoursSource.ESTIMATED, fire.getSchedule().getSource());
    }

    @Test
    void hoursAreUnknownWhenNothingCanBeGuessed() {
        Restaurant neapolis = importOne(entry("manual-58", Cuisine.OTHER, "", ""));

        assertEquals(HoursSource.UNKNOWN, neapolis.getSchedule().getSource());
    }

    @Test
    void locationComesFromTheLinkedMapPlace() {
        Restaurant khamees = importOne(entry("manual-63", Cuisine.SHAWARMA, "Mo-Su 07:00-00:00", "5347793623"),
                place("5347793623", ""));

        assertSame(KHAMEES_LOCATION, khamees.getLocation().orElseThrow());
    }

    @Test
    void entryWithoutMapLinkHasNoLocation() {
        Restaurant restaurant = importOne(entry("manual-01", Cuisine.OTHER, "Mo-Su 10:00-23:00", ""),
                place("5347793623", ""));

        assertTrue(restaurant.getLocation().isEmpty());
    }

    @Test
    void keepsIdNameCuisineAndOrder() {
        List<Restaurant> restaurants = importer.importRestaurants(List.of(
                entry("manual-02", Cuisine.CAFE, "", ""),
                entry("manual-01", Cuisine.SWEETS, "", "")), List.of());

        assertEquals(List.of("manual-02", "manual-01"), restaurants.stream().map(Restaurant::getId).toList());
        assertEquals("Restaurant manual-01", restaurants.get(1).getName());
        assertEquals(Cuisine.SWEETS, restaurants.get(1).getCuisine());
    }

    @Test
    void rejectsLinkToUnknownMapPlace() {
        List<ManualEntry> entries = List.of(entry("manual-01", Cuisine.OTHER, "", "999"));
        List<OsmPlace> places = List.of();

        DataFormatException error = assertThrows(DataFormatException.class,
                () -> importer.importRestaurants(entries, places));
        assertEquals("restaurants.txt: manual-01 links to unknown OpenStreetMap id 999", error.getMessage());
    }

    @Test
    void rejectsDuplicateTeamIds() {
        List<ManualEntry> entries = List.of(entry("manual-01", Cuisine.OTHER, "", ""),
                entry("manual-01", Cuisine.CAFE, "", ""));
        List<OsmPlace> places = List.of();

        assertThrows(DataFormatException.class, () -> importer.importRestaurants(entries, places));
    }

    @Test
    void rejectsDuplicateMapIds() {
        List<ManualEntry> entries = List.of();
        List<OsmPlace> places = List.of(place("1", ""), place("1", ""));

        assertThrows(DataFormatException.class, () -> importer.importRestaurants(entries, places));
    }

    @Test
    void needsAtLeastOneStrategy() {
        List<HoursStrategy> none = List.of();

        assertThrows(IllegalArgumentException.class, () -> new RestaurantImporter(none));
        assertThrows(IllegalArgumentException.class, () -> new RestaurantImporter(null));
    }

    @Test
    void stopsAtTheFirstStrategyThatKnowsTheHours() {
        WeeklySchedule allDay = WeeklySchedule.allDay(HoursSource.MANUAL);
        when(firstStrategy.resolve(any())).thenReturn(Optional.of(allDay));
        RestaurantImporter custom = new RestaurantImporter(List.of(firstStrategy, secondStrategy));

        Restaurant restaurant = custom.importRestaurants(
                List.of(entry("manual-01", Cuisine.OTHER, "", "")), List.of()).get(0);

        assertSame(allDay, restaurant.getSchedule());
        verify(secondStrategy, never()).resolve(any());
    }

    @Test
    void triesTheNextStrategyWhenOneDoesNotKnow() {
        WeeklySchedule allDay = WeeklySchedule.allDay(HoursSource.OSM);
        when(firstStrategy.resolve(any())).thenReturn(Optional.empty());
        when(secondStrategy.resolve(any())).thenReturn(Optional.of(allDay));
        RestaurantImporter custom = new RestaurantImporter(List.of(firstStrategy, secondStrategy));

        Restaurant restaurant = custom.importRestaurants(
                List.of(entry("manual-01", Cuisine.OTHER, "", "")), List.of()).get(0);

        assertSame(allDay, restaurant.getSchedule());
    }
}
