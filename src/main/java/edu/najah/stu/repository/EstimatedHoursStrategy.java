package edu.najah.stu.repository;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.HoursParser;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.WeeklySchedule;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Guesses typical hours from the kind of food, as a last resort.
 * The guessed hours are marked {@link HoursSource#ESTIMATED} so the
 * customer is told they may be wrong. No guess is made for {@link Cuisine#OTHER}.
 */
public class EstimatedHoursStrategy implements HoursStrategy {

    private static final Map<Cuisine, String> TYPICAL_HOURS = new EnumMap<>(Map.of(
            Cuisine.CAFE, "Mo-Su 08:00-00:00",
            Cuisine.SWEETS, "Mo-Su 08:00-23:00",
            Cuisine.SHAWARMA, "Mo-Su 10:00-02:00",
            Cuisine.BURGER, "Mo-Su 12:00-01:00",
            Cuisine.PIZZA, "Mo-Su 11:00-00:00",
            Cuisine.CHICKEN, "Mo-Su 11:00-00:00",
            Cuisine.GRILL, "Mo-Su 12:00-00:00",
            Cuisine.PALESTINIAN, "Mo-Su 09:00-23:00",
            Cuisine.SEAFOOD, "Mo-Su 12:00-23:00"));

    @Override
    public Optional<WeeklySchedule> resolve(ImportCandidate candidate) {
        return Optional.ofNullable(TYPICAL_HOURS.get(candidate.entry().cuisine()))
                .map(hours -> HoursParser.parse(hours, HoursSource.ESTIMATED));
    }
}
