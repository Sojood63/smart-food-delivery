package edu.najah.stu.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SmtpSettingsTest {

    @Test
    void removesExtraSpacesAndSpacesInsideThePassword() {
        SmtpSettings settings = new SmtpSettings(" smtp.gmail.com ", 587, " me@gmail.com ",
                "abcd efgh ijkl mnop", " me@gmail.com ");

        assertEquals("smtp.gmail.com", settings.host());
        assertEquals("me@gmail.com", settings.username());
        assertEquals("abcdefghijklmnop", settings.password());
        assertEquals("me@gmail.com", settings.from());
    }

    @Test
    void neverShowsThePassword() {
        SmtpSettings settings = new SmtpSettings("smtp-relay.brevo.com", 587, "login", "secret-key", "a@b.co");

        assertFalse(settings.toString().contains("secret-key"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void rejectsMissingValues(String value) {
        assertThrows(IllegalArgumentException.class, () -> new SmtpSettings(value, 587, "u", "p", "a@b.co"));
        assertThrows(IllegalArgumentException.class, () -> new SmtpSettings("h", 587, value, "p", "a@b.co"));
        assertThrows(IllegalArgumentException.class, () -> new SmtpSettings("h", 587, "u", value, "a@b.co"));
        assertThrows(IllegalArgumentException.class, () -> new SmtpSettings("h", 587, "u", "p", value));
    }

    @Test
    void rejectsPortsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> new SmtpSettings("h", 0, "u", "p", "a@b.co"));
        assertThrows(IllegalArgumentException.class, () -> new SmtpSettings("h", 65_536, "u", "p", "a@b.co"));
    }
}
