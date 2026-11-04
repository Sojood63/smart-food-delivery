package edu.najah.stu.notification;

import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The text of the account emails. A recording sender keeps the emails instead of sending them.
 */
class EmailNotificationServiceTest {

    private record Email(String to, String subject, String body) {
    }

    private final List<Email> sent = new ArrayList<>();
    private final EmailNotificationService service =
            new EmailNotificationService((to, subject, body) -> sent.add(new Email(to, subject, body)));
    private final User layan = new User("layan_99", "pbkdf2$1$c2FsdA==$aGFzaA==", Role.CUSTOMER, null,
            "layan@example.com");

    @Test
    void rejectsMissingSender() {
        assertThrows(IllegalArgumentException.class, () -> new EmailNotificationService(null));
    }

    @Test
    void welcomeGoesToTheCustomerByName() {
        service.welcome(layan);

        assertEquals(1, sent.size());
        Email email = sent.get(0);
        assertEquals("layan@example.com", email.to());
        assertEquals("Welcome to Smart Food Delivery", email.subject());
        assertTrue(email.body().startsWith("Hi layan_99,"));
        assertTrue(email.body().contains("near An-Najah"));
    }

    @Test
    void lockEmailSaysWhenToTryAgain() {
        service.accountLocked(layan, LocalDateTime.of(2026, 11, 4, 12, 15));

        Email email = sent.get(0);
        assertEquals("layan@example.com", email.to());
        assertEquals(EmailNotificationService.LOCKED_SUBJECT, email.subject());
        assertTrue(email.body().contains("3 times in a row"));
        assertTrue(email.body().contains("after 12:15 on 2026-11-04"), email.body());
    }

    @Test
    void aFailedEmailDoesNotStopTheProgram() {
        EmailNotificationService failing = new EmailNotificationService((to, subject, body) -> {
            throw new MailException("Could not send email to " + to, new IllegalStateException("offline"));
        });

        assertDoesNotThrow(() -> failing.welcome(layan));
        assertDoesNotThrow(() -> failing.accountLocked(layan, LocalDateTime.of(2026, 11, 4, 12, 15)));
    }
}
