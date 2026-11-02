package edu.najah.stu.repository;

/**
 * Thrown when a data file contains a line that cannot be read.
 * The message names the file and the line number so the line can be fixed.
 */
public class DataFormatException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param source     the file name, for example {@code "restaurants.txt"}
     * @param lineNumber the 1-based line number of the bad line
     * @param problem    what is wrong with the line
     */
    public DataFormatException(String source, int lineNumber, String problem) {
        super(source + " line " + lineNumber + ": " + problem);
    }

    /**
     * Creates the exception for a problem that is not tied to one line.
     *
     * @param source  the file name
     * @param problem what is wrong
     */
    public DataFormatException(String source, String problem) {
        super(source + ": " + problem);
    }
}
