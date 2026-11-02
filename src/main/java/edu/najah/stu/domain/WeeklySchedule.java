package edu.najah.stu.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * The opening hours of a restaurant for each day of the week.
 *
 * <p>A day with no hours is a closed day. Hours that run past midnight
 * belong to the day they start on: a Thursday shift from 18:00 to 02:00
 * keeps the restaurant open until 02:00 on Friday morning, even if Friday
 * itself is a closed day.</p>
 */
public final class WeeklySchedule {

    private final Map<DayOfWeek, OpeningHours> hoursByDay;
    private final boolean openAllDay;
    private final HoursSource source;

    private WeeklySchedule(Map<DayOfWeek, OpeningHours> hoursByDay, boolean openAllDay, HoursSource source) {
        if (source == null) {
            throw new IllegalArgumentException("Hours source must not be null");
        }
        this.hoursByDay = new EnumMap<>(DayOfWeek.class);
        this.hoursByDay.putAll(hoursByDay);
        this.openAllDay = openAllDay;
        this.source = source;
    }

    /**
     * Creates a schedule from the hours of each open day; missing days are closed.
     *
     * @param hoursByDay the hours of each open day; must not be null or hold null keys or values
     * @param source     where the hours came from; must not be null
     * @return the schedule
     * @throws IllegalArgumentException if an argument, day or hours value is null
     */
    public static WeeklySchedule of(Map<DayOfWeek, OpeningHours> hoursByDay, HoursSource source) {
        if (hoursByDay == null) {
            throw new IllegalArgumentException("Hours by day must not be null");
        }
        hoursByDay.forEach((day, hours) -> {
            if (day == null || hours == null) {
                throw new IllegalArgumentException("Day and hours must not be null");
            }
        });
        return new WeeklySchedule(hoursByDay, false, source);
    }

    /**
     * Creates a schedule with the same hours on all seven days.
     *
     * @param hours  the daily hours; must not be null
     * @param source where the hours came from; must not be null
     * @return the schedule
     */
    public static WeeklySchedule everyDay(OpeningHours hours, HoursSource source) {
        if (hours == null) {
            throw new IllegalArgumentException("Hours must not be null");
        }
        Map<DayOfWeek, OpeningHours> week = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            week.put(day, hours);
        }
        return new WeeklySchedule(week, false, source);
    }

    /**
     * Creates a schedule that is open 24 hours a day, every day.
     *
     * @param source where the hours came from; must not be null
     * @return the schedule
     */
    public static WeeklySchedule allDay(HoursSource source) {
        return new WeeklySchedule(Map.of(), true, source);
    }

    /**
     * Creates a schedule for a restaurant whose hours are not known.
     * Such a restaurant is never treated as open.
     *
     * @return the schedule
     */
    public static WeeklySchedule unknown() {
        return new WeeklySchedule(Map.of(), false, HoursSource.UNKNOWN);
    }

    /**
     * Tells whether the restaurant is open at a given moment, including
     * shifts that started the day before and run past midnight.
     *
     * @param moment the date and time to check; must not be null
     * @return {@code true} if the restaurant is open at that moment
     * @throws IllegalArgumentException if {@code moment} is null
     */
    public boolean isOpenAt(LocalDateTime moment) {
        if (moment == null) {
            throw new IllegalArgumentException("Moment must not be null");
        }
        return openAllDay || closingTime(moment).isPresent();
    }

    /**
     * Returns when the shift that is running at a given moment ends.
     *
     * @param moment the date and time to check; must not be null
     * @return the closing date and time; empty if closed at that moment or open all day
     * @throws IllegalArgumentException if {@code moment} is null
     */
    public Optional<LocalDateTime> closingTime(LocalDateTime moment) {
        if (moment == null) {
            throw new IllegalArgumentException("Moment must not be null");
        }
        if (openAllDay) {
            return Optional.empty();
        }
        LocalDate today = moment.toLocalDate();
        LocalTime time = moment.toLocalTime();

        OpeningHours yesterday = hoursByDay.get(today.minusDays(1).getDayOfWeek());
        if (yesterday != null && yesterday.closesOvernight() && time.isBefore(yesterday.getClosesAt())) {
            return Optional.of(today.atTime(yesterday.getClosesAt()));
        }
        OpeningHours todayHours = hoursByDay.get(today.getDayOfWeek());
        if (todayHours == null || time.isBefore(todayHours.getOpensAt())) {
            return Optional.empty();
        }
        if (todayHours.closesOvernight()) {
            return Optional.of(today.plusDays(1).atTime(todayHours.getClosesAt()));
        }
        if (time.isBefore(todayHours.getClosesAt())) {
            return Optional.of(today.atTime(todayHours.getClosesAt()));
        }
        return Optional.empty();
    }

    /**
     * Returns the hours of one day.
     *
     * @param day the day of the week
     * @return the hours, or empty if the restaurant is closed that day or open all day
     */
    public Optional<OpeningHours> hoursOn(DayOfWeek day) {
        return Optional.ofNullable(hoursByDay.get(day));
    }

    /**
     * Tells whether the restaurant never closes.
     *
     * @return {@code true} if open 24 hours a day, every day
     */
    public boolean isOpenAllDay() {
        return openAllDay;
    }

    /**
     * Returns where these hours came from.
     *
     * @return the hours source
     */
    public HoursSource getSource() {
        return source;
    }
}
