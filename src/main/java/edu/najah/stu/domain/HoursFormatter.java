package edu.najah.stu.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Writes a {@link WeeklySchedule} as text that {@link HoursParser} reads back
 * to the same schedule, for example {@code "Mo-Th,Sa-Su 10:00-23:00; Fr 12:00-23:00"}.
 */
public final class HoursFormatter {

    private static final String[] DAY_CODES = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
    private static final String ALL_DAYS_CLOSED = "Mo-Su off";

    private HoursFormatter() {
    }

    /**
     * Formats a schedule.
     *
     * @param schedule the schedule; must not be null
     * @return {@code "24/7"}, an empty text for unknown hours, or one rule per group of days
     * @throws IllegalArgumentException if the schedule is null
     */
    public static String format(WeeklySchedule schedule) {
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule must not be null");
        }
        if (schedule.isOpenAllDay()) {
            return "24/7";
        }
        if (schedule.getSource() == HoursSource.UNKNOWN) {
            return "";
        }
        Map<String, List<DayOfWeek>> daysByHours = new LinkedHashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            Optional<OpeningHours> hours = schedule.hoursOn(day);
            hours.ifPresent(value -> daysByHours.computeIfAbsent(timeRange(value), key -> new ArrayList<>()).add(day));
        }
        if (daysByHours.isEmpty()) {
            return ALL_DAYS_CLOSED;
        }
        List<String> rules = new ArrayList<>();
        daysByHours.forEach((range, days) -> rules.add(dayRanges(days) + " " + range));
        return String.join("; ", rules);
    }

    private static String timeRange(OpeningHours hours) {
        return time(hours.getOpensAt()) + "-" + time(hours.getClosesAt());
    }

    private static String time(LocalTime time) {
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }

    /** Joins days into ranges, for example Monday to Thursday plus Saturday and Sunday gives "Mo-Th,Sa-Su". */
    private static String dayRanges(List<DayOfWeek> days) {
        List<String> ranges = new ArrayList<>();
        int start = 0;
        for (int i = 1; i <= days.size(); i++) {
            boolean runEnds = i == days.size() || days.get(i).getValue() != days.get(i - 1).getValue() + 1;
            if (runEnds) {
                ranges.add(range(days.get(start), days.get(i - 1)));
                start = i;
            }
        }
        return String.join(",", ranges);
    }

    private static String range(DayOfWeek first, DayOfWeek last) {
        String from = DAY_CODES[first.getValue() - 1];
        return first.equals(last) ? from : from + "-" + DAY_CODES[last.getValue() - 1];
    }
}
