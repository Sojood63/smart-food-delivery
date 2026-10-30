package edu.najah.stu.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpeningHoursTest {

    private final OpeningHours dayHours = hours("09:00", "22:00");
    private final OpeningHours nightHours = hours("18:00", "02:00");

    private static OpeningHours hours(String opens, String closes) {
        return new OpeningHours(LocalTime.parse(opens), LocalTime.parse(closes));
    }

    // R1 / R2: normal day hours
    @Test
    void openDuringDayHours() {
        assertTrue(dayHours.isOpenAt(LocalTime.parse("12:00")));
    }

    @Test
    void closedAfterDayHours() {
        assertFalse(dayHours.isOpenAt(LocalTime.parse("23:00")));
    }

    @Test
    void closedBeforeDayHours() {
        assertFalse(dayHours.isOpenAt(LocalTime.parse("08:59")));
    }

    // R3 / R4: boundaries
    @Test
    void openExactlyAtOpeningTime() {
        assertTrue(dayHours.isOpenAt(LocalTime.parse("09:00")));
    }

    @Test
    void closedExactlyAtClosingTime() {
        assertFalse(dayHours.isOpenAt(LocalTime.parse("22:00")));
    }

    // R5 / R6: hours that run past midnight
    @Test
    void overnightOpenAfterMidnight() {
        assertTrue(nightHours.isOpenAt(LocalTime.parse("01:00")));
    }

    @Test
    void overnightOpenInTheEvening() {
        assertTrue(nightHours.isOpenAt(LocalTime.parse("20:00")));
    }

    @Test
    void overnightClosedAfterClosingTime() {
        assertFalse(nightHours.isOpenAt(LocalTime.parse("03:00")));
    }

    @Test
    void overnightClosedAtNoon() {
        assertFalse(nightHours.isOpenAt(LocalTime.parse("12:00")));
    }

    // R7 / R8: invalid input
    @Test
    void rejectsEqualTimes() {
        LocalTime nine = LocalTime.parse("09:00");
        assertThrows(IllegalArgumentException.class, () -> new OpeningHours(nine, nine));
    }

    @Test
    void rejectsNullOpeningTime() {
        LocalTime nine = LocalTime.parse("09:00");
        assertThrows(IllegalArgumentException.class, () -> new OpeningHours(null, nine));
    }

    @Test
    void rejectsNullClosingTime() {
        LocalTime nine = LocalTime.parse("09:00");
        assertThrows(IllegalArgumentException.class, () -> new OpeningHours(nine, null));
    }

    @Test
    void rejectsNullTimeWhenChecking() {
        assertThrows(IllegalArgumentException.class, () -> dayHours.isOpenAt(null));
    }

    // minutes until close (used for the "closing soon" warning)
    @Test
    void minutesUntilCloseOnSameDay() {
        assertEquals(20L, dayHours.minutesUntilClose(LocalTime.parse("21:40")));
    }

    @Test
    void minutesUntilCloseAcrossMidnight() {
        assertEquals(150L, nightHours.minutesUntilClose(LocalTime.parse("23:30")));
    }

    @Test
    void minutesUntilCloseAfterMidnight() {
        assertEquals(30L, nightHours.minutesUntilClose(LocalTime.parse("01:30")));
    }

    @Test
    void minutesUntilCloseFailsWhenClosed() {
        LocalTime late = LocalTime.parse("23:00");
        assertThrows(IllegalStateException.class, () -> dayHours.minutesUntilClose(late));
    }

    @Test
    void keepsOpeningAndClosingTimes() {
        assertEquals(LocalTime.parse("09:00"), dayHours.getOpensAt());
        assertEquals(LocalTime.parse("22:00"), dayHours.getClosesAt());
    }
}
