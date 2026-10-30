package edu.najah.stu.domain;

import java.time.Duration;
import java.time.LocalTime;

/**
 * The daily opening hours of a restaurant.
 *
 * <p>The opening time is inclusive and the closing time is exclusive:
 * a restaurant open from 09:00 to 22:00 is open at 09:00 and closed at 22:00.
 * Hours may run past midnight, for example from 18:00 to 02:00.</p>
 */
public final class OpeningHours {

    private static final long MINUTES_PER_DAY = 24L * 60;

    /** Time the restaurant opens every day (inclusive). */
    private final LocalTime opensAt;

    /** Time the restaurant closes every day (exclusive). */
    private final LocalTime closesAt;

    /**
     * Creates opening hours.
     *
     * @param opensAt  opening time; must not be null
     * @param closesAt closing time; must not be null or equal to {@code opensAt}
     * @throws IllegalArgumentException if a time is null or both times are equal
     */
    public OpeningHours(LocalTime opensAt, LocalTime closesAt) {
        if (opensAt == null || closesAt == null) {
            throw new IllegalArgumentException("Opening and closing times must not be null");
        }
        if (opensAt.equals(closesAt)) {
            throw new IllegalArgumentException("Opening and closing times must be different");
        }
        this.opensAt = opensAt;
        this.closesAt = closesAt;
    }

    /**
     * Tells whether the restaurant is open at the given time.
     *
     * @param time the time to check; must not be null
     * @return {@code true} if the restaurant is open at that time
     * @throws IllegalArgumentException if the time is null
     */
    public boolean isOpenAt(LocalTime time) {
        if (time == null) {
            throw new IllegalArgumentException("Time must not be null");
        }
        boolean afterOpening = !time.isBefore(opensAt);
        boolean beforeClosing = time.isBefore(closesAt);
        if (closesOvernight()) {
            return afterOpening || beforeClosing;
        }
        return afterOpening && beforeClosing;
    }

    /**
     * Returns how many minutes remain until the restaurant closes.
     *
     * @param time the current time; the restaurant must be open at this time
     * @return minutes until closing, always greater than zero
     * @throws IllegalStateException if the restaurant is closed at that time
     */
    public long minutesUntilClose(LocalTime time) {
        if (!isOpenAt(time)) {
            throw new IllegalStateException("Restaurant is closed at " + time);
        }
        long minutes = Duration.between(time, closesAt).toMinutes();
        return minutes > 0 ? minutes : minutes + MINUTES_PER_DAY;
    }

    /**
     * Returns the opening time.
     *
     * @return the opening time
     */
    public LocalTime getOpensAt() {
        return opensAt;
    }

    /**
     * Returns the closing time.
     *
     * @return the closing time
     */
    public LocalTime getClosesAt() {
        return closesAt;
    }

    private boolean closesOvernight() {
        return closesAt.isBefore(opensAt);
    }
}
