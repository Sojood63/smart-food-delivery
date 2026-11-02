package edu.najah.stu.repository;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipeFileTest {

    private static List<PipeFile.Row> read(String text, int columns) {
        return PipeFile.read(new BufferedReader(new StringReader(text)), "test.txt", columns);
    }

    @Test
    void readsFieldsSeparatedByPipes() {
        List<PipeFile.Row> rows = read("a|b|c\nd|e|f\n", 3);

        assertEquals(2, rows.size());
        assertEquals(List.of("a", "b", "c"), rows.get(0).fields());
        assertEquals("e", rows.get(1).field(1));
    }

    @Test
    void skipsCommentsAndBlankLinesButKeepsRealLineNumbers() {
        List<PipeFile.Row> rows = read("# header\n\n   \na|b\n# note\nc|d\n", 2);

        assertEquals(2, rows.size());
        assertEquals(4, rows.get(0).lineNumber());
        assertEquals(6, rows.get(1).lineNumber());
    }

    @Test
    void keepsEmptyFieldsIncludingTrailingOnes() {
        List<PipeFile.Row> rows = read("a||c|\n", 4);

        assertEquals(List.of("a", "", "c", ""), rows.get(0).fields());
    }

    @Test
    void trimsSpacesAroundFields() {
        assertEquals(List.of("Pizza Time", "PIZZA"), read("  Pizza Time |  PIZZA  ", 2).get(0).fields());
    }

    @Test
    void keepsArabicText() {
        assertEquals("مطعم خميس", read("manual-63|مطعم خميس", 2).get(0).field(1));
    }

    @Test
    void emptyFileGivesNoRows() {
        assertTrue(read("", 3).isEmpty());
    }

    @Test
    void wrongNumberOfFieldsNamesFileAndLine() {
        DataFormatException error = assertThrows(DataFormatException.class, () -> read("# header\na|b|c\na|b\n", 3));

        assertEquals("test.txt line 3: expected 3 fields but found 2", error.getMessage());
    }

    @Test
    void rowErrorPointsAtTheRow() {
        PipeFile.Row row = read("\na|b", 2).get(0);

        assertEquals("test.txt line 2: bad value", row.error("bad value").getMessage());
    }

    @Test
    void readFailureBecomesUncheckedIoException() {
        Reader broken = new Reader() {
            @Override
            public int read(char[] buffer, int offset, int length) throws IOException {
                throw new IOException("disk error");
            }

            @Override
            public void close() {
                // nothing to close
            }
        };
        BufferedReader reader = new BufferedReader(broken);

        assertThrows(UncheckedIOException.class, () -> PipeFile.read(reader, "test.txt", 2));
    }
}
