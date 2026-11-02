package edu.najah.stu.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HoursParserTest {

    private static Optional<String> hoursOn(WeeklySchedule schedule, DayOfWeek day) {
        return schedule.hoursOn(day).map(hours -> hours.getOpensAt() + "-" + hours.getClosesAt());
    }

    // P1
    @Test
    void sameHoursEveryDay() {
        WeeklySchedule schedule = HoursParser.parse("Mo-Su 09:00-23:00", HoursSource.OSM);

        for (DayOfWeek day : DayOfWeek.values()) {
            assertEquals(Optional.of("09:00-23:00"), hoursOn(schedule, day));
        }
        assertEquals(HoursSource.OSM, schedule.getSource());
    }

    @Test
    void hoursWithoutDaysMeanEveryDay() {
        WeeklySchedule schedule = HoursParser.parse("08:00-21:00", HoursSource.OSM);

        assertEquals(Optional.of("08:00-21:00"), hoursOn(schedule, DayOfWeek.FRIDAY));
        assertEquals(Optional.of("08:00-21:00"), hoursOn(schedule, DayOfWeek.MONDAY));
    }

    // P2
    @Test
    void laterRuleOverridesEarlierOne() {
        WeeklySchedule schedule = HoursParser.parse("Mo-Th,Sa-Su 10:00-23:00; Fr 12:00-23:00", HoursSource.MANUAL);

        assertEquals(Optional.of("10:00-23:00"), hoursOn(schedule, DayOfWeek.THURSDAY));
        assertEquals(Optional.of("12:00-23:00"), hoursOn(schedule, DayOfWeek.FRIDAY));
        assertEquals(Optional.of("10:00-23:00"), hoursOn(schedule, DayOfWeek.SUNDAY));
    }

    @Test
    void sameDayAppearingTwiceTakesTheLastRule() {
        WeeklySchedule schedule = HoursParser.parse("Mo-Su 10:00-00:00; Fr 16:00-00:00", HoursSource.OSM);

        assertEquals(Optional.of("16:00-00:00"), hoursOn(schedule, DayOfWeek.FRIDAY));
        assertEquals(Optional.of("10:00-00:00"), hoursOn(schedule, DayOfWeek.SATURDAY));
    }

    // P3
    @Test
    void offMeansClosed() {
        WeeklySchedule schedule = HoursParser.parse("Mo-Th,Sa-Su 09:00-23:00; Fr off", HoursSource.MANUAL);

        assertEquals(Optional.empty(), hoursOn(schedule, DayOfWeek.FRIDAY));
        assertEquals(Optional.of("09:00-23:00"), hoursOn(schedule, DayOfWeek.SATURDAY));
    }

    // P4
    @Test
    void twentyFourSeven() {
        WeeklySchedule schedule = HoursParser.parse("24/7", HoursSource.OSM);

        assertTrue(schedule.isOpenAllDay());
        assertEquals(HoursSource.OSM, schedule.getSource());
    }

    // P5: ranges that wrap around the end of the week
    @Test
    void rangeThatWrapsAroundTheWeek() {
        WeeklySchedule saturdayToThursday = HoursParser.parse("Sa-Th 07:00-22:00", HoursSource.OSM);
        WeeklySchedule sundayToSaturday = HoursParser.parse("Su-Sa 09:00-01:00", HoursSource.OSM);

        assertEquals(Optional.empty(), hoursOn(saturdayToThursday, DayOfWeek.FRIDAY));
        assertEquals(Optional.of("07:00-22:00"), hoursOn(saturdayToThursday, DayOfWeek.SUNDAY));
        assertEquals(Optional.of("07:00-22:00"), hoursOn(saturdayToThursday, DayOfWeek.THURSDAY));
        for (DayOfWeek day : DayOfWeek.values()) {
            assertEquals(Optional.of("09:00-01:00"), hoursOn(sundayToSaturday, day));
        }
    }

    @Test
    void acceptsSingleDigitHoursAndMidnightAs2400() {
        WeeklySchedule schedule = HoursParser.parse("Mo 9:00-24:00", HoursSource.MANUAL);

        assertEquals(Optional.of("09:00-00:00"), hoursOn(schedule, DayOfWeek.MONDAY));
    }

    @Test
    void ignoresCaseAndSurroundingSpaces() {
        WeeklySchedule schedule = HoursParser.parse("  mo-su 09:00-23:00  ", HoursSource.OSM);

        assertEquals(Optional.of("09:00-23:00"), hoursOn(schedule, DayOfWeek.WEDNESDAY));
    }

    // P6: broken or unsupported text becomes "unknown" instead of crashing
    @ParameterizedTest
    @ValueSource(strings = {
            "Sa-Th 11:00-23:00; Fr",
            "Mo-Su 09:00-12:00 PM",
            "Monday 09:00-17:00",
            "Mo-Su 25:00-26:00",
            "Mo-Su 09:00-09:00",
            "open daily",
            "Mo-Su 09:00-12:00,14:00-18:00"
    })
    void unsupportedTextGivesUnknownHours(String text) {
        WeeklySchedule schedule = HoursParser.parse(text, HoursSource.OSM);

        assertEquals(HoursSource.UNKNOWN, schedule.getSource());
        assertFalse(schedule.isOpenAllDay());
    }

    // P7
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void emptyTextGivesUnknownHours(String text) {
        assertEquals(HoursSource.UNKNOWN, HoursParser.parse(text, HoursSource.MANUAL).getSource());
    }

    @Test
    void parsedHoursWorkWithTheSchedule() {
        WeeklySchedule schedule = HoursParser.parse("Sa-Th 07:00-22:00", HoursSource.OSM);

        assertTrue(schedule.isOpenAt(LocalDateTime.of(2026, 10, 31, 12, 0)));
        assertFalse(schedule.isOpenAt(LocalDateTime.of(2026, 10, 30, 12, 0)));
        assertEquals(LocalTime.of(22, 0), schedule.hoursOn(DayOfWeek.MONDAY).orElseThrow().getClosesAt());
    }
}
