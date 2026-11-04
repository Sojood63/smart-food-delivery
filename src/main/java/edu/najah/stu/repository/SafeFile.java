package edu.najah.stu.repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Writes text files so that a crash in the middle never leaves a half
 * written file: the lines go to a temporary file first, which then replaces
 * the old file in one step.
 */
final class SafeFile {

    private SafeFile() {
    }

    /**
     * Replaces a file's content with the given lines, creating folders as needed.
     *
     * @param file  the file to write
     * @param lines the new content, written as UTF-8
     * @throws UncheckedIOException if the file cannot be written
     */
    static void write(Path file, List<String> lines) {
        try {
            Path folder = file.toAbsolutePath().getParent();
            Files.createDirectories(folder);
            Path temporary = Files.createTempFile(folder, file.getFileName().toString(), ".tmp");
            Files.write(temporary, lines, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write " + file, e);
        }
    }
}
