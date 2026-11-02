package edu.najah.stu.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HoursFormatterTest {

    private static String reformat(String text) {
        return HoursFormatter.format(HoursParser.parse(text, HoursSource.MANUAL));
    }

    @Test
    void sameHoursEveryDay() {
        assertEquals("Mo-Su 09:00-23:00", reformat("09:00-23:00"));
    }

    @Test
    void groupsDaysWithTheSameHours() {
        assertEquals("Mo-Th,Sa-Su 10:00-23:00; Fr 12:00-23:00",
                reformat("Mo-Su 10:00-23:00; Fr 12:00-23:00"));
    }

    @Test
    void leavesClosedDaysOut() {
        assertEquals("Mo-Th,Sa-Su 07:00-22:00", reformat("Sa-Th 07:00-22:00"));
    }

    @Test
    void singleDays() {
        assertEquals("Mo,We,Fr 09:00-17:00", reformat("Mo 09:00-17:00; We 09:00-17:00; Fr 09:00-17:00"));
    }

    @Test
    void writesSingleDigitHoursWithTwoDigits() {
        assertEquals("Mo-Su 09:00-00:00", reformat("Mo-Su 9:00-24:00"));
    }

    @Test
    void allDay() {
        assertEquals("24/7", HoursFormatter.format(WeeklySchedule.allDay(HoursSource.OSM)));
    }

    @Test
    void unknownHoursAreEmpty() {
        assertEquals("", HoursFormatter.format(WeeklySchedule.unknown()));
    }

    @Test
    void knownButAlwaysClosed() {
        WeeklySchedule closed = WeeklySchedule.of(Map.of(), HoursSource.MANUAL);

        assertEquals("Mo-Su off", HoursFormatter.format(closed));
        assertEquals(HoursSource.MANUAL, HoursParser.parse("Mo-Su off", HoursSource.MANUAL).getSource());
    }

    // F1: formatting then parsing gives the same hours back
    @ParameterizedTest
    @ValueSource(strings = {
            "Mo-Su 09:00-23:00",
            "Mo-Th,Sa-Su 10:00-23:00; Fr 12:00-23:00",
            "Mo-Th,Sa-Su 09:00-23:00",
            "Mo-Th,Sa-Su 11:30-01:00; Fr 12:30-02:00",
            "Mo-Fr,Su 09:00-23:00; Sa 08:00-23:00",
            "Th 18:00-02:00",
            "24/7"
    })
    void roundTrip(String text) {
        assertEquals(text, reformat(text));
        assertEquals(text, reformat(reformat(text)));
    }

    @Test
    void roundTripKeepsEveryDaysHours() {
        WeeklySchedule original = HoursParser.parse("Mo-Th,Sa-Su 11:30-01:00; Fr 12:30-02:00", HoursSource.OSM);
        WeeklySchedule copy = HoursParser.parse(HoursFormatter.format(original), HoursSource.OSM);

        for (DayOfWeek day : DayOfWeek.values()) {
            assertEquals(original.hoursOn(day).map(OpeningHours::getOpensAt), copy.hoursOn(day).map(OpeningHours::getOpensAt));
            assertEquals(original.hoursOn(day).map(OpeningHours::getClosesAt), copy.hoursOn(day).map(OpeningHours::getClosesAt));
        }
        assertEquals(LocalTime.of(2, 0), copy.hoursOn(DayOfWeek.FRIDAY).orElseThrow().getClosesAt());
    }

    @Test
    void rejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> HoursFormatter.format(null));
    }
}
