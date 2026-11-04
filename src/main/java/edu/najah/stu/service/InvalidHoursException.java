package edu.najah.stu.service;

/**
 * Thrown when a manager types opening hours that cannot be read.
 */
public class InvalidHoursException extends IllegalArgumentException {

    /** A correct example shown to the manager with every error. */
    public static final String EXAMPLE = "Sa-Th 09:00-23:00; Fr off";

    /**
     * Creates the exception.
     *
     * @param hoursText the text that could not be read
     */
    public InvalidHoursException(String hoursText) {
        super("Cannot read opening hours '" + hoursText + "'. Example: " + EXAMPLE);
    }
}
