package edu.najah.stu.repository;

import edu.najah.stu.domain.WeeklySchedule;

import java.util.Optional;

/**
 * One way of finding a restaurant's opening hours (Strategy pattern).
 *
 * <p>The importer tries its strategies in order of trust and uses the first
 * one that knows the hours, so a new source of hours can be added without
 * changing the importer.</p>
 */
@FunctionalInterface
public interface HoursStrategy {

    /**
     * Tries to find the hours of one restaurant.
     *
     * @param candidate the restaurant being imported and its map data, if any
     * @return the hours, or empty if this strategy does not know them
     */
    Optional<WeeklySchedule> resolve(ImportCandidate candidate);
}
