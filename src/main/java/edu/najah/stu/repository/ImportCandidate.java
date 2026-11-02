package edu.najah.stu.repository;

import java.util.Optional;

/**
 * A restaurant being imported: the team's entry and its OpenStreetMap place.
 *
 * @param entry       the team's entry; never null
 * @param linkedPlace the matching OpenStreetMap place, or null if the entry is not linked
 */
public record ImportCandidate(ManualEntry entry, OsmPlace linkedPlace) {

    /**
     * Returns the matching OpenStreetMap place.
     *
     * @return the place, or empty if the entry is not linked to the map
     */
    public Optional<OsmPlace> osmPlace() {
        return Optional.ofNullable(linkedPlace);
    }
}
