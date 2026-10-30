package edu.najah.stu.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;

class SystemTimeProviderTest {

    @Test
    void returnsCurrentTime() {
        LocalDateTime before = LocalDateTime.now();

        LocalDateTime result = new SystemTimeProvider().now();

        LocalDateTime after = LocalDateTime.now();
        assertFalse(result.isBefore(before));
        assertFalse(result.isAfter(after));
    }
}