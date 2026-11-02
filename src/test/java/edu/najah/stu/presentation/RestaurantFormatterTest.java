package edu.najah.stu.presentation;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.OpeningHours;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.WeeklySchedule;
import edu.najah.stu.service.NearbyRestaurant;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestaurantFormatterTest {

    private final Restaurant pizzaTime = new Restaurant("R1", "Pizza Time",
            WeeklySchedule.everyDay(new OpeningHours(LocalTime.of(11, 0), LocalTime.of(0, 0)), HoursSource.OSM),
            new Location(32.2241324, 35.2313647), Cuisine.PIZZA);

    // F1
    @Test
    void showsKilometresWithOneDecimal() {
        assertEquals("1.4 km", RestaurantFormatter.formatDistance(1.43));
        assertEquals("1.0 km", RestaurantFormatter.formatDistance(1.0));
    }

    // F2
    @Test
    void showsMetresBelowOneKilometreRoundedToTen() {
        assertEquals("350 m", RestaurantFormatter.formatDistance(0.35));
        assertEquals("350 m", RestaurantFormatter.formatDistance(0.3468));
        assertEquals("0 m", RestaurantFormatter.formatDistance(0));
    }

    @Test
    void switchesToKilometresWhenMetresRoundToOneThousand() {
        assertEquals("1.0 km", RestaurantFormatter.formatDistance(0.9996));
        assertEquals("990 m", RestaurantFormatter.formatDistance(0.994));
    }

    @Test
    void formatsAFullLine() {
        NearbyRestaurant result = new NearbyRestaurant(pizzaTime, 0.35, 5, false);

        assertEquals("Pizza Time | Pizza | ~350 m | ~5 min walk", RestaurantFormatter.format(result));
    }

    // F3
    @Test
    void addsClosingSoonWarning() {
        NearbyRestaurant result = new NearbyRestaurant(pizzaTime, 1.43, 18, true);

        assertEquals("Pizza Time | Pizza | ~1.4 km | ~18 min walk | closing soon!",
                RestaurantFormatter.format(result));
    }

    @Test
    void rejectsNullResult() {
        assertThrows(IllegalArgumentException.class, () -> RestaurantFormatter.format(null));
    }

    // W15
    @Test
    void marksEstimatedHours() {
        Restaurant fireShawarma = new Restaurant("R2", "FIRE Shawarma",
                WeeklySchedule.everyDay(new OpeningHours(LocalTime.of(10, 0), LocalTime.of(2, 0)),
                        HoursSource.ESTIMATED),
                new Location(32.22, 35.24), Cuisine.SHAWARMA);
        NearbyRestaurant result = new NearbyRestaurant(fireShawarma, 0.5, 6, false);

        assertEquals("FIRE Shawarma | Shawarma | ~500 m | ~6 min walk | (estimated hours)",
                RestaurantFormatter.format(result));
    }
}
