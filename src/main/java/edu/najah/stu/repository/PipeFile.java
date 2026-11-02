package edu.najah.stu.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reads text data files where each record is one line and fields are
 * separated by {@code |}. Blank lines and lines starting with {@code #}
 * are ignored, so files can carry comments.
 */
public final class PipeFile {

    private static final String SEPARATOR = "\\|";
    private static final String COMMENT = "#";

    private PipeFile() {
    }

    /**
     * One record of a data file.
     *
     * @param source     the file name, used in error messages
     * @param lineNumber the 1-based line number in the file
     * @param fields     the trimmed fields; empty fields are kept as empty strings
     */
    public record Row(String source, int lineNumber, List<String> fields) {

        /**
         * Returns one field.
         *
         * @param index the 0-based column index
         * @return the trimmed field text
         */
        public String field(int index) {
            return fields.get(index);
        }

        /**
         * Creates an exception that points at this row.
         *
         * @param problem what is wrong with the row
         * @return the exception, ready to throw
         */
        public DataFormatException error(String problem) {
            return new DataFormatException(source, lineNumber, problem);
        }
    }

    /**
     * Reads every record from a data file.
     *
     * @param reader  the file contents; closed by the caller
     * @param source  the file name, used in error messages
     * @param columns the number of fields every record must have
     * @return the records in file order
     * @throws DataFormatException if a record has the wrong number of fields
     * @throws UncheckedIOException if the file cannot be read
     */
    public static List<Row> read(BufferedReader reader, String source, int columns) {
        List<Row> rows = new ArrayList<>();
        int lineNumber = 0;
        try {
            String line = reader.readLine();
            while (line != null) {
                lineNumber++;
                String trimmed = line.strip();
                if (!trimmed.isEmpty() && !trimmed.startsWith(COMMENT)) {
                    rows.add(toRow(trimmed, source, lineNumber, columns));
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + source, e);
        }
        return rows;
    }

    private static Row toRow(String line, String source, int lineNumber, int columns) {
        List<String> fields = Arrays.stream(line.split(SEPARATOR, -1)).map(String::strip).toList();
        if (fields.size() != columns) {
            throw new DataFormatException(source, lineNumber,
                    "expected " + columns + " fields but found " + fields.size());
        }
        return new Row(source, lineNumber, fields);
    }
}
