package edu.najah.stu.domain;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeeklyScheduleTest {

    // 29 Oct 2026 is a Thursday, 30 Oct a Friday, 31 Oct a Saturday
    private static final LocalDateTime THURSDAY = LocalDateTime.of(2026, 10, 29, 0, 0);
    private static final LocalDateTime FRIDAY = LocalDateTime.of(2026, 10, 30, 0, 0);
    private static final LocalDateTime SATURDAY = LocalDateTime.of(2026, 10, 31, 0, 0);

    private static OpeningHours hours(String opens, String closes) {
        return new OpeningHours(LocalTime.parse(opens), LocalTime.parse(closes));
    }

    private static LocalDateTime at(LocalDateTime day, String time) {
        return day.with(LocalTime.parse(time));
    }

    /** Saturday to Thursday 07:00-22:00, closed on Friday (like Pizza Al Najah). */
    private static WeeklySchedule closedOnFriday() {
        Map<DayOfWeek, OpeningHours> week = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : EnumSet.complementOf(EnumSet.of(DayOfWeek.FRIDAY))) {
            week.put(day, hours("07:00", "22:00"));
        }
        return WeeklySchedule.of(week, HoursSource.OSM);
    }

    /** Open only on Thursday, from 18:00 until 02:00 on Friday morning. */
    private static WeeklySchedule thursdayNightOnly() {
        return WeeklySchedule.of(Map.of(DayOfWeek.THURSDAY, hours("18:00", "02:00")), HoursSource.MANUAL);
    }

    // W1
    @Test
    void openOnAWorkingDay() {
        assertTrue(closedOnFriday().isOpenAt(at(SATURDAY, "12:00")));
    }

    // W2
    @Test
    void closedOnAClosedDay() {
        assertFalse(closedOnFriday().isOpenAt(at(FRIDAY, "12:00")));
    }

    // W3 + W4
    @Test
    void usesTheSpecialHoursOfOneDay() {
        Map<DayOfWeek, OpeningHours> week = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            week.put(day, hours("10:00", "00:00"));
        }
        week.put(DayOfWeek.FRIDAY, hours("16:00", "00:00"));
        WeeklySchedule schedule = WeeklySchedule.of(week, HoursSource.OSM);

        assertFalse(schedule.isOpenAt(at(FRIDAY, "12:00")));
        assertTrue(schedule.isOpenAt(at(FRIDAY, "17:00")));
        assertTrue(schedule.isOpenAt(at(THURSDAY, "12:00")));
    }

    // W5: Thursday's night shift keeps it open on Friday morning
    @Test
    void openAfterMidnightFromYesterdaysShift() {
        assertTrue(thursdayNightOnly().isOpenAt(at(FRIDAY, "01:00")));
    }

    // W6
    @Test
    void closedOnceYesterdaysShiftEnds() {
        assertFalse(thursdayNightOnly().isOpenAt(at(FRIDAY, "03:00")));
        assertFalse(thursdayNightOnly().isOpenAt(at(FRIDAY, "02:00")));
    }

    // W7: a day shift does not carry over past midnight
    @Test
    void dayShiftDoesNotCarryOverToNextMorning() {
        WeeklySchedule schedule = WeeklySchedule.of(
                Map.of(DayOfWeek.THURSDAY, hours("09:00", "22:00")), HoursSource.MANUAL);

        assertFalse(schedule.isOpenAt(at(FRIDAY, "01:00")));
    }

    @Test
    void openInTheEveningBeforeMidnight() {
        assertTrue(thursdayNightOnly().isOpenAt(at(THURSDAY, "23:00")));
        assertFalse(thursdayNightOnly().isOpenAt(at(THURSDAY, "17:59")));
        assertFalse(thursdayNightOnly().isOpenAt(at(THURSDAY, "01:00")));
    }

    // W8
    @Test
    void allDayIsAlwaysOpen() {
        WeeklySchedule schedule = WeeklySchedule.allDay(HoursSource.OSM);

        assertTrue(schedule.isOpenAt(at(FRIDAY, "03:00")));
        assertTrue(schedule.isOpenAt(at(SATURDAY, "15:00")));
        assertTrue(schedule.isOpenAllDay());
    }

    // W9: an all-day place never has a closing time
    @Test
    void allDayHasNoClosingTime() {
        assertEquals(Optional.empty(), WeeklySchedule.allDay(HoursSource.OSM).closingTime(at(FRIDAY, "23:50")));
    }

    // W10
    @Test
    void unknownHoursAreNeverOpen() {
        WeeklySchedule schedule = WeeklySchedule.unknown();

        assertFalse(schedule.isOpenAt(at(FRIDAY, "12:00")));
        assertEquals(HoursSource.UNKNOWN, schedule.getSource());
    }

    // W11: the closing time of a shift that started yesterday
    @Test
    void closingTimeOfYesterdaysShift() {
        assertEquals(Optional.of(at(FRIDAY, "02:00")), thursdayNightOnly().closingTime(at(FRIDAY, "01:45")));
    }

    @Test
    void closingTimeOfTonightsShiftIsTomorrow() {
        assertEquals(Optional.of(at(FRIDAY, "02:00")), thursdayNightOnly().closingTime(at(THURSDAY, "20:00")));
    }

    @Test
    void closingTimeOfADayShiftIsToday() {
        assertEquals(Optional.of(at(SATURDAY, "22:00")), closedOnFriday().closingTime(at(SATURDAY, "21:40")));
    }

    @Test
    void noClosingTimeWhenClosed() {
        assertEquals(Optional.empty(), closedOnFriday().closingTime(at(SATURDAY, "23:00")));
        assertEquals(Optional.empty(), closedOnFriday().closingTime(at(SATURDAY, "06:00")));
    }

    // W12
    @Test
    void everyDayGivesTheSameHoursToAllSevenDays() {
        OpeningHours daily = hours("09:00", "23:00");
        WeeklySchedule schedule = WeeklySchedule.everyDay(daily, HoursSource.MANUAL);

        for (DayOfWeek day : DayOfWeek.values()) {
            assertEquals(Optional.of(daily), schedule.hoursOn(day));
        }
        assertEquals(HoursSource.MANUAL, schedule.getSource());
        assertFalse(schedule.isOpenAllDay());
    }

    @Test
    void closedDayHasNoHours() {
        assertEquals(Optional.empty(), closedOnFriday().hoursOn(DayOfWeek.FRIDAY));
    }

    @Test
    void changingTheOriginalMapDoesNotChangeTheSchedule() {
        Map<DayOfWeek, OpeningHours> week = new EnumMap<>(DayOfWeek.class);
        week.put(DayOfWeek.MONDAY, hours("09:00", "17:00"));
        WeeklySchedule schedule = WeeklySchedule.of(week, HoursSource.MANUAL);

        week.put(DayOfWeek.FRIDAY, hours("09:00", "17:00"));

        assertEquals(Optional.empty(), schedule.hoursOn(DayOfWeek.FRIDAY));
    }

    // W13
    @Test
    void rejectsMissingValues() {
        Map<DayOfWeek, OpeningHours> nullHours = new HashMap<>();
        nullHours.put(DayOfWeek.MONDAY, null);
        Map<DayOfWeek, OpeningHours> nullDay = new HashMap<>();
        nullDay.put(null, hours("09:00", "17:00"));

        assertThrows(IllegalArgumentException.class, () -> WeeklySchedule.of(null, HoursSource.MANUAL));
        assertThrows(IllegalArgumentException.class, () -> WeeklySchedule.of(nullHours, HoursSource.MANUAL));
        assertThrows(IllegalArgumentException.class, () -> WeeklySchedule.of(nullDay, HoursSource.MANUAL));
        assertThrows(IllegalArgumentException.class, () -> WeeklySchedule.everyDay(null, HoursSource.MANUAL));
        assertThrows(IllegalArgumentException.class, () -> WeeklySchedule.allDay(null));
        assertThrows(IllegalArgumentException.class, () -> closedOnFriday().isOpenAt(null));
        assertThrows(IllegalArgumentException.class, () -> closedOnFriday().closingTime(null));
    }
}
