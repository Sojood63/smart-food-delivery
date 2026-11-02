package edu.najah.stu.repository;

import edu.najah.stu.domain.HoursParser;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.WeeklySchedule;

import java.util.Optional;

/**
 * Uses the hours the team collected itself. This is the most trusted source.
 */
public class ManualHoursStrategy implements HoursStrategy {

    @Override
    public Optional<WeeklySchedule> resolve(ImportCandidate candidate) {
        WeeklySchedule schedule = HoursParser.parse(candidate.entry().hoursText(), HoursSource.MANUAL);
        return schedule.getSource() == HoursSource.UNKNOWN ? Optional.empty() : Optional.of(schedule);
    }
}
