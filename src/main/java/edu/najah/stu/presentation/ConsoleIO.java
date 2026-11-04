package edu.najah.stu.presentation;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * Prints prompts and reads typed answers, one line at a time.
 */
final class ConsoleIO {

    private final Scanner in;
    private final PrintStream out;

    /**
     * Creates the helper.
     *
     * @param in  where answers are read from; must not be null
     * @param out where text is printed; must not be null
     */
    ConsoleIO(Scanner in, PrintStream out) {
        if (in == null || out == null) {
            throw new IllegalArgumentException("Input and output must not be null");
        }
        this.in = in;
        this.out = out;
    }

    /** Prints one line of text. */
    void println(String text) {
        out.println(text);
    }

    /**
     * Prints a prompt and reads one line.
     *
     * @return the trimmed answer, or null when there is no more input
     */
    String ask(String prompt) {
        out.println(prompt);
        return in.hasNextLine() ? in.nextLine().trim() : null;
    }

    /**
     * Like {@link #ask(String)}, but no more input counts as an empty answer.
     *
     * @return the trimmed answer, never null
     */
    String answer(String prompt) {
        String reply = ask(prompt);
        return reply == null ? "" : reply;
    }
}
