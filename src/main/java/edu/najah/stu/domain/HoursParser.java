package edu.najah.stu.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads opening hours written in the common OpenStreetMap style.
 *
 * <p>Supported forms:</p>
 * <ul>
 *   <li>{@code 24/7}</li>
 *   <li>{@code 09:00-23:00} (every day)</li>
 *   <li>{@code Mo-Su 09:00-01:00}, {@code Sa-Th 07:00-22:00} (ranges may wrap around the week)</li>
 *   <li>{@code Mo-Th,Sa-Su 10:00-23:00; Fr 12:00-23:00} (later rules override earlier ones)</li>
 *   <li>{@code Mo-Th,Sa-Su 09:00-23:00; Fr off} (a closed day)</li>
 * </ul>
 * <p>Anything else gives an unknown schedule instead of an error, because
 * real map data is often incomplete or mistyped.</p>
 */
public final class HoursParser {

    private static final Map<String, DayOfWeek> DAY_CODES = Map.of(
            "mo", DayOfWeek.MONDAY, "tu", DayOfWeek.TUESDAY, "we", DayOfWeek.WEDNESDAY,
            "th", DayOfWeek.THURSDAY, "fr", DayOfWeek.FRIDAY, "sa", DayOfWeek.SATURDAY,
            "su", DayOfWeek.SUNDAY);
    private static final Pattern TIME_RANGE = Pattern.compile("(\\d{1,2}:\\d{2})-(\\d{1,2}:\\d{2})");
    private static final String CLOSED = "off";
    private static final int DAYS_PER_WEEK = 7;

    private HoursParser() {
    }

    /**
     * Parses an opening-hours text.
     *
     * @param text   the hours text, for example {@code "Sa-Th 07:00-22:00"}; may be null
     * @param source where the text came from
     * @return the schedule, or {@link WeeklySchedule#unknown()} if the text is empty or not understood
     */
    public static WeeklySchedule parse(String text, HoursSource source) {
        if (text == null || text.isBlank()) {
            return WeeklySchedule.unknown();
        }
        String normalised = text.trim().toLowerCase(Locale.ROOT);
        if (normalised.equals("24/7")) {
            return WeeklySchedule.allDay(source);
        }
        Map<DayOfWeek, OpeningHours> week = new EnumMap<>(DayOfWeek.class);
        for (String rule : normalised.split(";")) {
            if (!applyRule(rule.trim(), week)) {
                return WeeklySchedule.unknown();
            }
        }
        return WeeklySchedule.of(week, source);
    }

    /**
     * Applies one rule such as {@code "Mo-Th,Sa-Su 10:00-23:00"} or {@code "Fr off"}.
     *
     * @return {@code false} if the rule is not understood
     */
    private static boolean applyRule(String rule, Map<DayOfWeek, OpeningHours> week) {
        String[] parts = rule.split("\\s+");
        if (parts.length > 2 || parts[0].isEmpty()) {
            return false;
        }
        List<DayOfWeek> days = parts.length == 1 ? List.of(DayOfWeek.values()) : parseDays(parts[0]);
        if (days.isEmpty()) {
            return false;
        }
        String hoursText = parts[parts.length - 1];
        if (hoursText.equals(CLOSED)) {
            days.forEach(week::remove);
            return true;
        }
        Optional<OpeningHours> hours = parseHours(hoursText);
        hours.ifPresent(value -> days.forEach(day -> week.put(day, value)));
        return hours.isPresent();
    }

    /**
     * Parses a list of days such as {@code "mo-th,sa-su"}.
     *
     * @return the days, or an empty list if any part is not a valid day or range
     */
    private static List<DayOfWeek> parseDays(String daysText) {
        List<DayOfWeek> days = new ArrayList<>();
        for (String part : daysText.split(",")) {
            String[] ends = part.split("-");
            DayOfWeek from = DAY_CODES.get(ends[0]);
            DayOfWeek to = ends.length == 2 ? DAY_CODES.get(ends[1]) : from;
            if (ends.length > 2 || from == null || to == null) {
                return List.of();
            }
            int count = Math.floorMod(to.getValue() - from.getValue(), DAYS_PER_WEEK) + 1;
            for (int offset = 0; offset < count; offset++) {
                days.add(from.plus(offset));
            }
        }
        return days;
    }

    /**
     * Parses a time range such as {@code "10:00-23:00"}.
     *
     * @return the hours, or empty if the text is not a valid range
     */
    private static Optional<OpeningHours> parseHours(String hoursText) {
        Matcher matcher = TIME_RANGE.matcher(hoursText);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new OpeningHours(time(matcher.group(1)), time(matcher.group(2))));
        } catch (DateTimeParseException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static LocalTime time(String text) {
        String padded = text.length() == 4 ? "0" + text : text;
        return padded.equals("24:00") ? LocalTime.MIDNIGHT : LocalTime.parse(padded);
    }
}
