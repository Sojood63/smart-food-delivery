package edu.najah.stu.repository;

import edu.najah.stu.domain.HoursParser;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.WeeklySchedule;

import java.util.Optional;

/**
 * Uses the {@code opening_hours} of the linked OpenStreetMap place.
 */
public class OsmHoursStrategy implements HoursStrategy {

    @Override
    public Optional<WeeklySchedule> resolve(ImportCandidate candidate) {
        return candidate.osmPlace()
                .map(place -> HoursParser.parse(place.hoursText(), HoursSource.OSM))
                .filter(schedule -> schedule.getSource() != HoursSource.UNKNOWN);
    }
}
