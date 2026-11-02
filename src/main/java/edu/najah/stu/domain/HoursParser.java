package edu.najah.stu.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

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

    private static final List<String> DAY_CODES = List.of("mo", "tu", "we", "th", "fr", "sa", "su");
    private static final String DAY = "(mo|tu|we|th|fr|sa|su)";
    private static final Pattern DAY_PART = Pattern.compile(DAY + "(?:-" + DAY + ")?");
    private static final Pattern RULE = Pattern.compile(
            "(?:((?:" + DAY + "(?:-" + DAY + ")?)(?:," + DAY + "(?:-" + DAY + ")?)*)\\s+)?"
                    + "(off|(\\d{1,2}:\\d{2})-(\\d{1,2}:\\d{2}))");

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

    private static boolean applyRule(String rule, Map<DayOfWeek, OpeningHours> week) {
        Matcher matcher = RULE.matcher(rule);
        if (!matcher.matches()) {
            return false;
        }
        List<DayOfWeek> days = matcher.group(1) == null ? List.of(DayOfWeek.values()) : days(matcher.group(1));
        if (matcher.group(6).equals("off")) {
            days.forEach(week::remove);
            return true;
        }
        try {
            OpeningHours hours = new OpeningHours(time(matcher.group(7)), time(matcher.group(8)));
            days.forEach(day -> week.put(day, hours));
            return true;
        } catch (DateTimeParseException | IllegalArgumentException e) {
            return false;
        }
    }

    private static List<DayOfWeek> days(String daysText) {
        return DAY_PART.matcher(daysText).results()
                .flatMap(part -> {
                    int from = DAY_CODES.indexOf(part.group(1));
                    int to = part.group(2) == null ? from : DAY_CODES.indexOf(part.group(2));
                    int count = Math.floorMod(to - from, DAY_CODES.size()) + 1;
                    return IntStream.range(0, count)
                            .mapToObj(offset -> DayOfWeek.of((from + offset) % DAY_CODES.size() + 1));
                })
                .toList();
    }

    private static LocalTime time(String text) {
        String padded = text.length() == 4 ? "0" + text : text;
        return padded.equals("24:00") ? LocalTime.MIDNIGHT : LocalTime.parse(padded);
    }
}
