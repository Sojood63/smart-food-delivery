package edu.najah.stu.service;

import java.time.LocalDateTime;

/**
 * {@link TimeProvider} that returns the real time of the machine.
 */
public class SystemTimeProvider implements TimeProvider {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}