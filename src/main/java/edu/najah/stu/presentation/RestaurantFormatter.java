package edu.najah.stu.presentation;

import edu.najah.stu.service.NearbyRestaurant;

import java.util.Locale;

/**
 * Turns nearby-restaurant results into one-line text for the console.
 *
 * <p>Example: {@code Pizza Time | Pizza | ~350 m | ~5 min walk | closing soon!}</p>
 */
public final class RestaurantFormatter {

    private static final double METRES_PER_KM = 1000;
    private static final long ROUND_METRES_TO = 10;

    private RestaurantFormatter() {
    }

    /**
     * Formats one search result.
     *
     * @param result the result to format; must not be null
     * @return a single line describing the restaurant, its distance and walking time
     */
    public static String format(NearbyRestaurant result) {
        if (result == null) {
            throw new IllegalArgumentException("Result must not be null");
        }
        String line = result.restaurant().getName()
                + " | " + result.restaurant().getCuisine().getDisplayName()
                + " | ~" + formatDistance(result.distanceKm())
                + " | ~" + result.walkingMinutes() + " min walk";
        return result.closingSoon() ? line + " | closing soon!" : line;
    }

    /**
     * Formats a distance: metres below one kilometre, otherwise kilometres with one decimal.
     *
     * @param distanceKm the distance in kilometres
     * @return the distance as text, for example {@code "350 m"} or {@code "1.4 km"}
     */
    static String formatDistance(double distanceKm) {
        long metres = Math.round(distanceKm * METRES_PER_KM / ROUND_METRES_TO) * ROUND_METRES_TO;
        if (metres < METRES_PER_KM) {
            return metres + " m";
        }
        return String.format(Locale.ROOT, "%.1f km", distanceKm);
    }
}
